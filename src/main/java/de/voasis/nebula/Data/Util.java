package de.voasis.nebula.Data;

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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Callable;

public class Util {

    static ProxyServer server;
    private final Logger logger = LoggerFactory.getLogger("nebula");
    private MiniMessage mm = MiniMessage.miniMessage();
    static Object plugin;
    private static final int LENGTH = 5;
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final Set<String> generatedStrings = new HashSet<>();
    private static final Random random = new Random();

    public Util(ProxyServer server, Object plugin) {
        Util.server = server;
        Util.plugin = plugin;
    }

    public static String generateUniqueString() {
        StringBuilder sb;
        String result;
        do {
            sb = new StringBuilder(LENGTH);
            for (int i = 0; i < LENGTH; i++) {
                int index = random.nextInt(CHARACTERS.length());
                sb.append(CHARACTERS.charAt(index));
            }
            result = sb.toString();
        } while (generatedStrings.contains(result));
        generatedStrings.add(result);
        return result;
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
                externalServer.setFreePort(freePort);
            }
        } catch (Exception e) {
            logger.error("Failed to fetch free port via SSH on server: {}", externalServer.getServerName());
        }
    }

    public void updateState() {
        for (BackendServer backendServer : Data.backendInfoMap) {
            Optional<RegisteredServer> registeredServer = server.getServer(backendServer.getServerName());
            registeredServer.ifPresent(value -> pingServer(value, stateComplete(value), stateCompleteFailed(value), logger, plugin));
        }
    }

    public Callable<Void> stateComplete(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : Data.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (!backendServer.isOnline()) {
                        backendServer.setOnline(true);
                        CommandSource creator = backendServer.getCreator();
                        for(Player p : backendServer.getPendingPlayerConnections()) {
                            connectPlayer(p, backendServer, false);
                        }
                        sendMessage(creator, Messages.ONLINE.replace("<name>", backendServer.getServerName()));
                    }
                }
            }
            return null;
        };
    }

    public Callable<Void> stateCompleteFailed(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : Data.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (backendServer.isOnline()) {
                        backendServer.setOnline(false);
                        CommandSource creator = backendServer.getCreator();
                        sendMessage(creator, Messages.OFFLINE.replace("<name>", backendServer.getServerName()));
                    }
                }
            }
            return null;
        };
    }

    public void connectPlayer(Player player, BackendServer backendServer, boolean quit) {
        String name = backendServer.getServerName();
        Optional<RegisteredServer> target = server.getServer(name);
        player.sendMessage(mm.deserialize(Messages.SERVER_CONNECT.replace("<name>", name)));
        if(target.isPresent()) {
            player.createConnectionRequest(target.get()).fireAndForget();
            return;
        }
        if(quit) {
            player.disconnect(Component.empty());
        }
    }

    public boolean isInAnyQueue(Player player) {
        return Data.gamemodeQueueMap.stream()
                .anyMatch(queue -> queue.getInQueue().contains(player));
    }

    public void joinQueue(Player player, String queueName) {
        if (isInAnyQueue(player)) {
            player.sendMessage(mm.deserialize(Messages.ALREADY_IN_QUEUE));
            return;
        }
        if(!Objects.equals(Nebula.util.getBackendServer(player.getCurrentServer().get().getServerInfo().getName()).getTag(), "lobby")) {
            player.sendMessage(mm.deserialize(Messages.LOBBY_ONLY));
            return;
        }
        Data.gamemodeQueueMap.stream()
                .filter(queue -> queue.getName().equalsIgnoreCase(queueName))
                .findFirst()
                .ifPresentOrElse(
                        queue -> {
                            queue.getInQueue().add(player);
                            player.sendMessage(mm.deserialize(Messages.ADDED_TO_QUEUE.replace("<queue>", queueName)));
                        },
                        () -> player.sendMessage(mm.deserialize(Messages.QUEUE_NOT_FOUND))
                );
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

    public BackendServer getBackendServer(String name) {
        for (BackendServer server : Data.backendInfoMap) {
            if (server.getServerName().equals(name)) {
                return server;
            }
        }
        return null;
    }

    public int getPlayerCount(Object backendServer) {
        if(backendServer instanceof RegisteredServer registeredServer && backendServer != null) {
            return registeredServer.getPlayersConnected().size();
        }
        if(backendServer instanceof BackendServer backend && backendServer != null) {
            return server.getServer(backend.getServerName()).get().getPlayersConnected().size();
        }
        return 0;
    }

    private String stripColorCodes(String message) {
        return message.replaceAll("<.*?>", "");
    }

    public void sendMessage(CommandSource source, String message) {
        source = source != null ? source : server.getConsoleCommandSource();
        logger.info(stripColorCodes(message));
        if (source != server.getConsoleCommandSource()) {
            source.sendMessage(mm.deserialize(message));
        }
    }
}
