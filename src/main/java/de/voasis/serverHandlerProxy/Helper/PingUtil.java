package de.voasis.serverHandlerProxy.Helper;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;

public class PingUtil {
    static DataHolder dataHolder;
    static ProxyServer server;
    static final Logger logger = LoggerFactory.getLogger("serverhandlerproxy");
    static Object plugin;

    public PingUtil(DataHolder dataHolder, ProxyServer server, Object plugin) {
        PingUtil.dataHolder = dataHolder;
        PingUtil.server = server;
        PingUtil.plugin = plugin;
    }


    public void updateFreePort(ServerInfo externalServer) {
        int freePort = -1;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(externalServer.getUsername(), externalServer.getIp(), 22);
            session.setPassword(externalServer.getPassword());

            // SSH settings
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            // Command to find a free port between 5000 and 5100
            String command = "ruby -e 'require \"socket\"; puts Addrinfo.tcp(\"\", 0).bind {|s| s.local_address.ip_port }'";

            // Execute the command
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);

            InputStream in = channelExec.getInputStream();
            channelExec.connect();

            // Read the result of the command
            byte[] tmp = new byte[1024];
            int i = in.read(tmp, 0, 1024);
            if (i != -1) {
                freePort = Integer.parseInt(new String(tmp, 0, i).trim());
            }

            channelExec.disconnect();
            session.disconnect();

            if (freePort > 0) {
                logger.info("Free port received via SSH: " + freePort);
                externalServer.setFreePort(freePort);
            } else {
                logger.error("No free port found via SSH.");
            }
        } catch (Exception e) {
            logger.error("Failed to fetch free port via SSH.", e);
        }
    }

    public void updateState() {
        for (BackendServer backendServer : dataHolder.backendInfoMap) {
            Optional<RegisteredServer> registeredServer = server.getServer(backendServer.getServerName());
            registeredServer.ifPresent(value -> pingServer(value, stateComplete(value), stateCompleteFailed(value), logger, plugin));
        }
    }

    public Callable<Void> stateComplete(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (!backendServer.isOnline()) {
                        backendServer.setOnline(true);
                        logger.info("Server: " + backendServer.getServerName() + ", is now online.");
                        CommandSource creator = backendServer.getCreator();
                        for(Player p : backendServer.getPendingPlayerConnections()) {
                            RegisteredServer target = server.getServer(backendServer.getServerName()).get();
                            if(target != null) {
                                p.createConnectionRequest(target).fireAndForget();
                            }
                        }
                        if (creator != null) {
                            creator.sendMessage(Component.text("Server: " + backendServer.getServerName() + " is now online.", NamedTextColor.GREEN));
                        }
                    }
                }
            }
            return null;
        };
    }

    public Callable<Void> stateCompleteFailed(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (backendServer.isOnline()) {
                        backendServer.setOnline(false);
                        CommandSource creator = backendServer.getCreator();
                        logger.info("Server: " + backendServer.getServerName() + ", is now offline.");
                        if (creator != null) {
                            creator.sendMessage(Component.text("Server: " + backendServer.getServerName() + " is now offline.", NamedTextColor.RED));
                        }
                    }
                }
            }
            return null;
        };
    }

    public void pingServer(RegisteredServer regServer, Callable<Void> response, Callable<Void> noResponse, Logger logger, Object plugin) {
        regServer.ping().whenComplete((result, exception) -> {
            if (exception == null) {
                try {
                    synchronized (plugin) {
                        response.call();
                    }
                } catch (Exception e) {
                    logger.error("Error while executing success response for server: " + regServer.getServerInfo().getName(), e);
                }
            } else {
                try {
                    synchronized (plugin) {
                        noResponse.call();
                    }
                } catch (Exception e) {
                    logger.error("Error while executing failure response for server: " + regServer.getServerInfo().getName(), e);
                }
            }
        });
    }
}
