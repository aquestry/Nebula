package de.voasis.nebula;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Helper.DataHolder;
import de.voasis.nebula.Helper.PingUtil;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Helper.Messages;
import de.voasis.nebula.Maps.QueueInfo;
import de.voasis.nebula.Maps.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;

public class ExternalServerManager {
    private Logger logger;
    private ProxyServer server;
    private DataHolder dataHolder;
    private PingUtil pingUtil;

    public ExternalServerManager(Logger logger, ProxyServer proxyServer, DataHolder dataHolder, PingUtil pingUtil) {
        this.logger = logger;
        this.server = proxyServer;
        this.dataHolder = dataHolder;
        this.pingUtil = pingUtil;
    }

    private void executeSSHCommand(ServerInfo externalServer, String command, CommandSource source, String successMessage, String errorMessage) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(externalServer.getUsername(), externalServer.getIp(), 22);
            session.setPassword(externalServer.getPassword());

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);

            InputStream in = channelExec.getInputStream();
            channelExec.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channelExec.isClosed()) {
                    if (channelExec.getExitStatus() == 0) {
                        sendSuccessMessage(source, successMessage);

                    } else {
                        sendErrorMessage(source, errorMessage);
                    }
                    break;
                }
                Thread.sleep(1000);
            }

            channelExec.disconnect();
            session.disconnect();
        } catch (Exception e) {
            logger.error("Failed to execute SSH command.", e);
            sendErrorMessage(source, errorMessage);
        }
    }

    public void createFromTemplate(ServerInfo externalServer, String templateName, String newName, CommandSource source) {
        for (BackendServer backendServer : dataHolder.backendInfoMap) {
            if(backendServer.getServerName().equals(newName)) {
                sendErrorMessage(source, "Server already exists.");
                return;
            }
        }
        int tempPort = externalServer.getFreePort();
        String command = String.format("docker run -d -p %d:25565 --name %s %s SECRET=%s", tempPort, newName, templateName, Messages.vsecret);

        executeSSHCommand(externalServer, command, source,
                "Docker container created from Docker Hub template: " + templateName,
                "Failed to create Docker container from Docker Hub template.");

        com.velocitypowered.api.proxy.server.ServerInfo newInfo = new com.velocitypowered.api.proxy.server.ServerInfo(newName, new InetSocketAddress(externalServer.getIp(), tempPort));
        server.registerServer(newInfo);
        dataHolder.backendInfoMap.add(new BackendServer(newName, externalServer, tempPort, false, source, templateName));
        pingUtil.updateFreePort(externalServer);
        for(QueueInfo q : dataHolder.queues) {
            if(q.getGamemode().getTemplateName().equals(templateName)) {
                q.setUsed(false);
            }
        }

    }

    public void kill(ServerInfo externalServer, String servername, CommandSource source) {
        for(Player p : server.getServer(servername).get().getPlayersConnected()) {
            p.createConnectionRequest(dataHolder.defaultRegisteredServer).fireAndForget();
            p.sendMessage(Component.text(Messages.killed, NamedTextColor.GOLD));
        }
        String command = "docker kill " + servername;
        executeSSHCommand(externalServer, command, source, "Docker container stopped: " + servername, "Failed to stop Docker container.");

    }

    public void delete(ServerInfo externalServer, String servername, CommandSource source) {
        kill(externalServer, servername, source);
        String command = "docker rm -f " + servername;
        executeSSHCommand(externalServer, command, source, "Docker container deleted: " + servername, "Failed to delete Docker container.");
        server.unregisterServer(new com.velocitypowered.api.proxy.server.ServerInfo(servername, new InetSocketAddress(externalServer.getIp(), dataHolder.getBackendServer(servername).getPort())));
        dataHolder.backendInfoMap.remove(dataHolder.getBackendServer(servername));
    }

    private void sendSuccessMessage(CommandSource source, String message) {
        logger.info(message);
        if (source != null) {
            source.sendMessage(Component.text(message, NamedTextColor.GREEN));
        }
    }

    private void sendErrorMessage(CommandSource source, String message) {
        logger.error(message);
        if (source != null) {
            source.sendMessage(Component.text(message, NamedTextColor.RED));
        }
    }
}
