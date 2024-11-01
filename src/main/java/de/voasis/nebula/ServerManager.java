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

    private boolean executeSSHCommand(HoldServer externalServer, String command, Runnable onSuccess, Runnable onError) {
        Session session = null;
        ChannelExec channelExec = null;
        try {
            session = new JSch().getSession(externalServer.getUsername(), externalServer.getIp(), 22);
            session.setPassword(externalServer.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            InputStream in = channelExec.getInputStream();
            channelExec.connect();
            byte[] buffer = new byte[1024];
            while (!channelExec.isClosed()) {
                while (in.available() > 0) {
                    logger.info(new String(buffer, 0, in.read(buffer)));
                }
                Thread.sleep(100);
            }
            boolean success = channelExec.getExitStatus() == 0;
            if (success) {
                onSuccess.run();
            } else {
                onError.run();
            }
            return success;
        } catch (Exception e) {
            onError.run();
            return false;
        } finally {
            if (channelExec != null) channelExec.disconnect();
            if (session != null) session.disconnect();
        }
    }

    public BackendServer createFromTemplate(String templateName, String newName, CommandSource source, String tag) {
        HoldServer externalServer = Data.holdServerMap.getFirst();
        for (BackendServer backendServer : Data.backendInfoMap) {
            if (backendServer.getServerName().equals(newName)) {
                Nebula.util.sendMessage(source, Messages.ALREADY_EXISTS.replace("<name>", newName));
                return null;
            }
        }
        int tempPort = externalServer.getFreePort();
        String command = String.format("docker run -d -e PAPER_VELOCITY_SECRET=%s -p %d:25565 --name %s %s",
                Data.vsecret, tempPort, newName, templateName);
        Nebula.util.sendMessage(source, Messages.CREATE_CONTAINER.replace("<name>", newName));
        executeSSHCommand(externalServer, command,
                () -> {
                    ServerInfo newInfo = new ServerInfo(newName, new InetSocketAddress(externalServer.getIp(), tempPort));
                    server.registerServer(newInfo);
                    BackendServer backendServer = new BackendServer(newName, externalServer, tempPort, false, source, templateName, tag);
                    Data.backendInfoMap.add(backendServer);
                    Nebula.util.updateFreePort(externalServer);
                    Nebula.util.sendMessage(source, Messages.DONE);
                },
                () -> Nebula.util.sendMessage(source, Messages.ERROR_CREATE.replace("<name>", newName))
        );
        return null;
    }

    public void kill(BackendServer serverToDelete, CommandSource source) {
        source = source != null ? source : server.getConsoleCommandSource();
        if (serverToDelete == null) return;
        String name = serverToDelete.getServerName();
        server.getServer(name).ifPresent(serverInfo -> {
            for (Player p : serverInfo.getPlayersConnected()) {
                Optional<RegisteredServer> target = server.getServer(Nebula.defaultsManager.getTarget().getServerName());
                if (target.isPresent()) {
                    p.createConnectionRequest(target.get()).fireAndForget();
                } else {
                    p.disconnect(Component.empty());
                }
            }
        });
        Nebula.util.sendMessage(source, Messages.KILL_CONTAINER.replace("<name>", name));
        CommandSource finalSource = source;
        executeSSHCommand(serverToDelete.getHoldServer(), "docker kill " + name,
                () -> Nebula.util.sendMessage(finalSource, Messages.DONE),
                () -> Nebula.util.sendMessage(finalSource, Messages.ERROR_KILL.replace("<name>", name))
        );
    }

    public void pull(HoldServer externalServer, String template, CommandSource source) {
        String externName = externalServer.getServerName();
        executeSSHCommand(externalServer, "docker pull " + template,
                () -> {
                    Nebula.util.sendMessage(source, Messages.PULL_TEMPLATE.replace("<name>", externName).replace("<template>", template));
                    Nebula.util.sendMessage(source, Messages.DONE);
                },
                () -> Nebula.util.sendMessage(source, Messages.ERROR_PULL.replace("<name>", externName).replace("<template>", template))
        );
    }

    public void delete(BackendServer serverToDelete, CommandSource source) {
        source = source != null ? source : server.getConsoleCommandSource();
        if (serverToDelete == null) {
            Nebula.util.sendMessage(source, Messages.SERVER_NOT_FOUND.replace("<name>", serverToDelete.getServerName()));
            return;
        }
        kill(serverToDelete, source);
        String name = serverToDelete.getServerName();
        HoldServer externalServer = serverToDelete.getHoldServer();
        Nebula.util.sendMessage(source, Messages.DELETE_CONTAINER.replace("<name>", name));
        CommandSource finalSource = source;
        executeSSHCommand(externalServer, "docker rm -f " + name,
                () -> {
                    server.unregisterServer(new ServerInfo(name, new InetSocketAddress(externalServer.getIp(), serverToDelete.getPort())));
                    Data.backendInfoMap.remove(serverToDelete);
                    Nebula.util.sendMessage(finalSource, Messages.DONE);
                },
                () -> Nebula.util.sendMessage(finalSource, Messages.ERROR_DELETE.replace("<name>", name))
        );
    }
}
