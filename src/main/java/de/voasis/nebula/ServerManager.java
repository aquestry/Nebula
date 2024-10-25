package de.voasis.nebula;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.HoldServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;

public class ServerManager {
    private final Logger logger = LoggerFactory.getLogger("nebula");
    private final ProxyServer server;

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
                source.sendMessage(Component.text(successMessage, NamedTextColor.GREEN));
            } else {
                source.sendMessage(Component.text(errorMessage, NamedTextColor.GOLD));
            }
            channelExec.disconnect();
            session.disconnect();
        } catch (Exception e) {
            logger.error("Failed to execute SSH command.", e);
            source.sendMessage(Component.text(errorMessage, NamedTextColor.GOLD));
        }
    }

    public BackendServer createFromTemplate(String templateName, String newName, CommandSource source, String tag) {
        HoldServer externalServer = Data.holdServerMap.getFirst();
        for (BackendServer backendServer : Data.backendInfoMap) {
            if (backendServer.getServerName().equals(newName)) {
                source.sendMessage(Component.text("Server already exists.", NamedTextColor.GOLD));
                return null;
            }
        }
        int tempPort = externalServer.getFreePort();
        String command = String.format("docker run -d -p %d:25565 --name %s %s SECRET=%s",
                tempPort, newName, templateName, Data.vsecret);
        executeSSHCommand(externalServer, command, source,
                "Container created from template: " + templateName,
                "Failed to create container: " + newName);
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
                p.disconnect(Component.text("The server you were on was killed.", NamedTextColor.GOLD));
            }
        });
        executeSSHCommand(serverToDelete.getHoldServer(), "docker kill " + name,
                source, "Docker container killed: " + name,
                "Failed to kill Docker container.");
    }

    public void pull(HoldServer externalServer, String template) {
        executeSSHCommand(externalServer, "docker pull " + template,
                null , "Docker template pulled: " + template,
                "Failed to pull Docker template: " + template);
    }

    public void delete(BackendServer serverToDelete, CommandSource source) {
        source = source != null ? source : server.getConsoleCommandSource();
        if (serverToDelete == null) {
            source.sendMessage(Component.text("Server not found", NamedTextColor.RED));
            return;
        }
        String name = serverToDelete.getServerName();
        HoldServer externalServer = serverToDelete.getHoldServer();
        Data.backendInfoMap.removeIf(bs -> bs.getServerName().equals(name));
        kill(serverToDelete, source);
        executeSSHCommand(externalServer, "docker rm -f " + name,
                source, "Docker container deleted: " + name,
                "Failed to delete Docker container.");
        server.unregisterServer(new ServerInfo(name,
                new InetSocketAddress(externalServer.getIp(), serverToDelete.getPort())));
    }
}