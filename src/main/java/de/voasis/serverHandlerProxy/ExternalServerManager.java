package de.voasis.serverHandlerProxy;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.serverHandlerProxy.Helper.DataHolder;
import de.voasis.serverHandlerProxy.Helper.PingUtil;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Maps.Messages;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
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
            int tempPort = dataHolder.getServerInfo(externalServer.getServerName()).getFreePort();
            logger.info("Template Method, V-Secret: " + Messages.vsecret);
            String command =  "docker run -d -p " + tempPort + ":25565 " + templateName + " SECRET=" + Messages.vsecret;
            executeSSHCommand(externalServer, command, source,
                    "Docker container created from Docker Hub template: " + templateName,
                    "Failed to create Docker container from Docker Hub template.");

            com.velocitypowered.api.proxy.server.ServerInfo newInfo = new com.velocitypowered.api.proxy.server.ServerInfo(
                    newName, new InetSocketAddress(externalServer.getIp(), tempPort));
            server.registerServer(newInfo);
            dataHolder.backendInfoMap.add(new BackendServer(newInfo.getName(), externalServer.getServerName(), externalServer.getFreePort(), false, source));
            pingUtil.updateFreePort(dataHolder.getServerInfo(externalServer.getServerName()));
    }

    public void kill(ServerInfo externalServer, String servername, CommandSource source) {
        String command = "docker kill " + servername;
        executeSSHCommand(externalServer, command, source, "Docker container stopped: " + servername, "Failed to stop Docker container.");

    }

    public void delete(ServerInfo externalServer, String servername, CommandSource source) {
        kill(externalServer, servername, source);
        String command = "docker rm -f " + servername;
        executeSSHCommand(externalServer, command, source, "Docker container deleted: " + servername, "Failed to delete Docker container.");
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
