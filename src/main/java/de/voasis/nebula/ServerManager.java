package de.voasis.nebula;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.HoldServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;

public class ServerManager {
    private final Logger logger = LoggerFactory.getLogger("nebula");
    private final ProxyServer server;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ServerManager(ProxyServer proxyServer) {
        this.server = proxyServer;
    }

    private void executeSSHCommand(HoldServer externalServer, String command, CommandSource source, String successMessage, String errorMessage) {
        source = source != null ? source : server.getConsoleCommandSource();
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
                source.sendMessage(mm.deserialize(successMessage));
            } else {
                source.sendMessage(mm.deserialize(errorMessage));
            }
            channelExec.disconnect();
            session.disconnect();
        } catch (Exception e) {
            logger.error("Failed to execute SSH command.", e);
            source.sendMessage(mm.deserialize(errorMessage));
        }
    }

    public BackendServer createFromTemplate(String templateName, String newName, CommandSource source, String tag) {
        if (Data.holdServerMap.isEmpty()) {
            source.sendMessage(mm.deserialize(Messages.ERROR_SERVER_NOT_FOUND));
            return null;
        }

        HoldServer externalServer = Data.holdServerMap.getFirst();
        for (BackendServer backendServer : Data.backendInfoMap) {
            if (backendServer.getServerName().equals(newName)) {
                source.sendMessage(mm.deserialize(Messages.FEEDBACK_SERVER_EXISTS));
                return null;
            }
        }

        int tempPort = externalServer.getFreePort();
        String command = String.format("docker run -d -e PAPER_VELOCITY_SECRET=%s -p %d:25565 --name %s %s",
                Data.vsecret, tempPort, newName, templateName);
        executeSSHCommand(externalServer, command, source,
                Messages.FEEDBACK_TEMPLATE_CREATE.replace("<template>", templateName),
                Messages.ERROR_CONTAINER_FAILED.replace("<container>", newName));

        ServerInfo newInfo = new ServerInfo(newName, new InetSocketAddress(externalServer.getIp(), tempPort));
        BackendServer backendServer = new BackendServer(newName, externalServer, tempPort, false, source, templateName, tag);
        Data.backendInfoMap.add(backendServer);
        Nebula.util.updateFreePort(externalServer);
        server.registerServer(newInfo);
        return backendServer;
    }

    public void kill(BackendServer serverToKill, CommandSource source) {
        String name = serverToKill.getServerName();
        server.getServer(name).ifPresent(serverInfo -> {
            for (Player p : serverInfo.getPlayersConnected()) {
                p.disconnect(mm.deserialize(Messages.ERROR_SERVER_KILLED));
            }
        });
        executeSSHCommand(serverToKill.getHoldServer(), "docker kill " + name,
                source, Messages.FEEDBACK_KILL_START.replace("<container>", name),
                Messages.ERROR_CONTAINER_FAILED.replace("<container>", name));
    }

    public void pull(HoldServer externalServer, String template) {
        executeSSHCommand(externalServer, "docker pull " + template,
                null, Messages.FEEDBACK_TEMPLATE_CREATE.replace("<template>", template),
                Messages.ERROR_CONTAINER_FAILED.replace("<container>", template));
    }

    public void delete(BackendServer serverToDelete, CommandSource source) {
        source = source != null ? source : server.getConsoleCommandSource();
        if (serverToDelete == null) {
            source.sendMessage(mm.deserialize(Messages.ERROR_SERVER_NOT_FOUND));
            return;
        }
        String name = serverToDelete.getServerName();
        HoldServer externalServer = serverToDelete.getHoldServer();
        Data.backendInfoMap.removeIf(bs -> bs.getServerName().equals(name));
        kill(serverToDelete, source);
        executeSSHCommand(externalServer, "docker rm -f " + name,
                source, Messages.FEEDBACK_DELETE_START.replace("<container>", name),
                Messages.ERROR_CONTAINER_FAILED.replace("<container>", name));
        server.unregisterServer(new ServerInfo(name,
                new InetSocketAddress(externalServer.getIp(), serverToDelete.getPort())));
    }
}
