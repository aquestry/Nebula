package de.voasis.nebula;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.HoldServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Optional;

public class ServerManager {
    private final Logger logger = LoggerFactory.getLogger("nebula");
    private final ProxyServer server;
    private MiniMessage mm = MiniMessage.miniMessage();

    public ServerManager(ProxyServer proxyServer) {
        this.server = proxyServer;
    }

    private void executeSSHCommand(HoldServer externalServer, String command, CommandSource source, String successMessage, String errorMessage) {
        boolean isConsoleSource = source == server.getConsoleCommandSource();
        try {
            Session session = new JSch().getSession(externalServer.getUsername(), externalServer.getIp(), 22);
            session.setPassword(externalServer.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            InputStream in = channelExec.getInputStream();
            channelExec.connect();
            byte[] buffer = new byte[1024];
            while (!channelExec.isClosed()) {
                while (in.available() > 0) {
                    System.out.print(new String(buffer, 0, in.read(buffer)));
                }
                Thread.sleep(500);
            }
            if (channelExec.getExitStatus() == 0) {
                if (source != null) {
                    source.sendMessage(isConsoleSource ? Component.text(stripColorCodes(successMessage)) : Component.text(successMessage, NamedTextColor.GREEN));
                } else {
                    logger.info(stripColorCodes(successMessage));
                }
            } else {
                if (source != null) {
                    source.sendMessage(isConsoleSource ? Component.text(stripColorCodes(errorMessage)) : Component.text(errorMessage, NamedTextColor.GOLD));
                } else {
                    logger.info(stripColorCodes(errorMessage));
                }
            }
            channelExec.disconnect();
            session.disconnect();
        } catch (Exception e) {
            logger.error("Failed to execute SSH command.", e);
            if (source != null) {
                source.sendMessage(isConsoleSource ? Component.text(stripColorCodes(errorMessage)) : Component.text(errorMessage, NamedTextColor.GOLD));
            } else {
                logger.info(stripColorCodes(errorMessage));
            }
        }
    }

    private String stripColorCodes(String message) {
        return message.replaceAll("<.*?>", "");
    }

    public BackendServer createFromTemplate(String templateName, String newName, CommandSource source, String tag) {
        HoldServer externalServer = Data.holdServerMap.getFirst();
        for (BackendServer backendServer : Data.backendInfoMap) {
            if (backendServer.getServerName().equals(newName)) {
                source.sendMessage(mm.deserialize(Messages.ALREADY_EXISTS));
                return null;
            }
        }
        int tempPort = externalServer.getFreePort();
        String command = String.format("docker run -d -e PAPER_VELOCITY_SECRET=%s -p %d:25565 --name %s %s",
                Data.vsecret, tempPort, newName, templateName);
        executeSSHCommand(externalServer, command, source,
                Messages.CREATE_CONTAINER.replace("<name>", newName),
                Messages.ERROR_CREATE.replace("<name>", newName)
                );
        ServerInfo newInfo = new ServerInfo(newName, new InetSocketAddress(externalServer.getIp(), tempPort));
        server.registerServer(newInfo);
        BackendServer backendServer = new BackendServer(newName, externalServer, tempPort, false, source, templateName, tag);
        Data.backendInfoMap.add(backendServer);
        Nebula.util.updateFreePort(externalServer);
        return backendServer;
    }

    public void kill(BackendServer serverToDelete, CommandSource source) {
        String name = serverToDelete.getServerName();
        server.getServer(name).ifPresent(serverInfo -> {
            for (Player p : serverInfo.getPlayersConnected()) {
                Optional<RegisteredServer> target = server.getServer(Nebula.defaultsManager.getTarget().getServerName());
                if(target.isPresent()) {
                    p.createConnectionRequest(target.get()).fireAndForget();
                } else {
                    p.disconnect(Component.empty());
                }
            }
        });
        executeSSHCommand(serverToDelete.getHoldServer(), "docker kill " + name,
                source,
                Messages.KILL_CONTAINER.replace("<name>", name),
                Messages.ERROR_KILL.replace("<name>", name)
        );
    }

    public void pull(HoldServer externalServer, String template) {
        String externName = externalServer.getServerName();
        executeSSHCommand(externalServer, "docker pull " + template,
                null ,
                Messages.PULL_TEMPLATE.replace("<name>", externName).replace("<template>", template),
                Messages.ERROR_PULL.replace("<name>", externName).replace("<template>", template)
                );
    }

    public void delete(BackendServer serverToDelete, CommandSource source) {
        source = source != null ? source : server.getConsoleCommandSource();
        if (serverToDelete == null) {
            source.sendMessage(mm.deserialize(Messages.SERVER_NOT_FOUND));
            return;
        }
        String name = serverToDelete.getServerName();
        HoldServer externalServer = serverToDelete.getHoldServer();
        Data.backendInfoMap.removeIf(bs -> bs.getServerName().equals(name));
        kill(serverToDelete, source);
        executeSSHCommand(externalServer, "docker rm -f " + name,
                source,
                Messages.DELETE_CONTAINER.replace("<name>", name),
                Messages.ERROR_DELETE.replace("<name>", name)
                );
        server.unregisterServer(new ServerInfo(name,
                new InetSocketAddress(externalServer.getIp(),
                        serverToDelete.getPort())));
    }
}