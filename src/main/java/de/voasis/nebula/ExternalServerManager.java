package de.voasis.nebula;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.voasis.nebula.Helper.DataHolder;
import de.voasis.nebula.Helper.Util;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.QueueInfo;
import de.voasis.nebula.Maps.HoldServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.InetSocketAddress;

public class ExternalServerManager {
    private static final Logger logger = LoggerFactory.getLogger("nebula");
    private final ProxyServer server;
    private final DataHolder dataHolder;
    private final Util util;

    public ExternalServerManager( ProxyServer proxyServer) {
        this.server = proxyServer;
        this.dataHolder = Nebula.dataHolder;
        this.util = Nebula.util;
    }

    public void executeSSHCommand(HoldServer externalServer, String command, CommandSource source, String successMessage, String errorMessage) {
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


    public void createFromTemplate(HoldServer externalServer, String templateName, String newName, CommandSource source, String tag) {
        for (BackendServer backendServer : dataHolder.backendInfoMap) {
            if(backendServer.getServerName().equals(newName)) {
                source.sendMessage(Component.text("Server already exists.", NamedTextColor.GOLD));
                return;
            }
        }
        int tempPort = externalServer.getFreePort();
        ServerInfo newInfo = new ServerInfo(newName, new InetSocketAddress(externalServer.getIp(), tempPort));
        server.registerServer(newInfo);
        dataHolder.backendInfoMap.add(new BackendServer(newName, externalServer, tempPort, false, source, templateName, tag));
        String command = "docker run -d -p " + tempPort + ":25565 --name " + newName + " " + templateName + " SECRET=" + Data.vsecret;
        executeSSHCommand(externalServer, command, source,
                "Container created from template: " + templateName,
                "Failed to create container.");
        util.updateFreePort(externalServer);
        for(QueueInfo q : dataHolder.queues) {
            if(q.getGamemode().getTemplateName().equals(templateName)) {
                q.setUsed(false);
            }
        }
    }

    public void kill(HoldServer externalServer, String servername, CommandSource source) {
        for(Player p : server.getServer(servername).get().getPlayersConnected()) {
            p.createConnectionRequest(Nebula.defaultManager.getDefault()).fireAndForget();
            p.sendMessage(Component.text("The server you were on was killed.", NamedTextColor.GOLD));
        }
        String command = "docker kill " + servername;
        executeSSHCommand(externalServer, command, source, "Docker container killed: " + servername, "Failed to kill Docker container.");
    }

    public void delete(HoldServer externalServer, String servername, CommandSource source) {
        kill(externalServer, servername, source);
        String command = "docker rm -f " + servername;
        executeSSHCommand(externalServer, command, source, "Docker container deleted: " + servername, "Failed to delete Docker container.");
        server.unregisterServer(new ServerInfo(servername, new InetSocketAddress(externalServer.getIp(), dataHolder.getBackendServer(servername).getPort())));
        dataHolder.backendInfoMap.remove(dataHolder.getBackendServer(servername));
    }
}
