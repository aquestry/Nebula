package de.voasis.nebula.Helper;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.HoldServer;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Callable;

public class Util {
    static ProxyServer server;
    private final Logger logger = LoggerFactory.getLogger("nebula");
    static Object plugin;

    public Util(ProxyServer server, Object plugin) {
        Util.server = server;
        Util.plugin = plugin;
    }

    public void updateFreePort(HoldServer externalServer) {
        int freePort = -1;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(externalServer.getUsername(), externalServer.getIp(), 22);
            session.setPassword(externalServer.getPassword());
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            String command = "ruby -e 'require \"socket\"; puts Addrinfo.tcp(\"\", 0).bind {|s| s.local_address.ip_port }'";
            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            InputStream in = channelExec.getInputStream();
            channelExec.connect();
            byte[] tmp = new byte[1024];
            int i = in.read(tmp, 0, 1024);
            if (i != -1) {
                freePort = Integer.parseInt(new String(tmp, 0, i).trim());
            }
            channelExec.disconnect();
            session.disconnect();
            if (freePort > 0) {
                logger.info("Free port received via SSH: {}", freePort);
                externalServer.setFreePort(freePort);
            } else {
                logger.error("No free port found via SSH.");
            }
        } catch (Exception e) {
            logger.error("Failed to fetch free port via SSH.", e);
        }
    }

    public void updateState() {
        for (BackendServer backendServer : Nebula.dataHolder.backendInfoMap) {
            Optional<RegisteredServer> registeredServer = server.getServer(backendServer.getServerName());
            registeredServer.ifPresent(value -> pingServer(value, stateComplete(value), stateCompleteFailed(value), logger, plugin));
        }
    }

    public Callable<Void> stateComplete(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : Nebula.dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (!backendServer.isOnline()) {
                        backendServer.setOnline(true);
                        CommandSource creator = backendServer.getCreator();
                        for(Player p : backendServer.getPendingPlayerConnections()) {
                            RegisteredServer target = server.getServer(backendServer.getServerName()).get();
                            p.createConnectionRequest(target).fireAndForget();
                        }
                        creator.sendMessage(Component.text("Server: " + backendServer.getServerName() + " is now online.", NamedTextColor.GREEN));
                    }
                }
            }
            return null;
        };
    }

    public Callable<Void> stateCompleteFailed(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : Nebula.dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (backendServer.isOnline()) {
                        backendServer.setOnline(false);
                        CommandSource creator = backendServer.getCreator();
                        creator.sendMessage(Component.text("Server: " + backendServer.getServerName() + " is now offline.", NamedTextColor.GOLD));
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
                    logger.error("Error while executing success response for server: {}", regServer.getServerInfo().getName(), e);
                }
            } else {
                try {
                    synchronized (plugin) {
                        noResponse.call();
                    }
                } catch (Exception e) {
                    logger.error("Error while executing failure response for server: {}", regServer.getServerInfo().getName(), e);
                }
            }
        });
    }

    public static <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Random r = new Random();
        int i = r.nextInt(list.size());
        return list.get(i);
    }
}
