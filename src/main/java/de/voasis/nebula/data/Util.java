package de.voasis.nebula.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.map.Container;
import de.voasis.nebula.map.Group;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.map.Party;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Util {

    private MiniMessage mm = MiniMessage.miniMessage();
    private static final int LENGTH = 5;
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static HashMap<CommandSource, String> lastMessages = new HashMap<>();
    private static final Set<String> generatedStrings = new HashSet<>();
    private static final Random random = new Random();

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

    private void stateComplete(RegisteredServer registeredServer) {
        for (Container container : Data.backendInfoMap) {
            if (registeredServer.getServerInfo().getName().equals(container.getServerName())) {
                synchronized (container) {
                    if (!container.isOnline()) {
                        container.setOnline(true);
                        callPending(container);
                        CommandSource creator = container.getCreator();
                        sendMessage(creator, Messages.ONLINE.replace("<name>", container.getServerName()));
                    }
                }
            }
        }
    }

    public void stateCompleteFailed(RegisteredServer registeredServer) {
        for (Container container : Data.backendInfoMap) {
            if (registeredServer.getServerInfo().getName().equals(container.getServerName())) {
                synchronized (container) {
                    if (container.isOnline()) {
                        checkLobbys(true);
                        container.setOnline(false);
                        CommandSource creator = container.getCreator();
                        sendMessage(creator, Messages.OFFLINE.replace("<name>", container.getServerName()));
                    }
                }
            }
        }
    }

    public void pingServers() {
        checkLobbys(false);
        for (Container container : new ArrayList<>(Data.backendInfoMap)) {
            Optional<RegisteredServer> registeredServer = Nebula.server.getServer(container.getServerName());
            registeredServer.ifPresent(regServer -> regServer.ping().whenComplete((result, exception) -> {
                if (exception == null) {
                    try {
                        stateComplete(regServer);
                    } catch (Exception e) {
                        log("Error while executing success response for server: {}", regServer.getServerInfo().getName());
                    }
                } else {
                    try {
                        stateCompleteFailed(regServer);
                    } catch (Exception e) {
                        log("Error while executing failure response for server: {}", regServer.getServerInfo().getName());
                    }
                }
            }));
        }
    }

    public void checkLobbys(boolean online) {
        int lobbys = 0;
        for(Container container : Data.backendInfoMap) {
            if(container.getFlags().contains("lobby")) {
                if(online && container.isOnline()) {
                    lobbys++;
                } else if(!online){
                    lobbys++;
                }
            }
        }
        if(lobbys == 0) {
            Nebula.defaultsManager.createDefault();
            log("No lobby server found, creating new one...");
        }
    }

    public void connectPlayer(Player player, Container container, boolean quit) {
        if(container == null) {
            if(quit) {
                player.disconnect(Component.empty());
            }
            return;
        }
        String name = container.getServerName();
        Optional<RegisteredServer> target = Nebula.server.getServer(name);
        sendMessage(player, Messages.SERVER_CONNECT.replace("<name>", name));
        if(target.isPresent()) {
            player.createConnectionRequest(target.get()).fireAndForget();
            return;
        }
        if(quit) {
            player.disconnect(Component.empty());
        }
    }

    public Container getBackendServer(String name) {
        for (Container server : Data.backendInfoMap) {
            if (server.getServerName().equals(name)) {
                return server;
            }
        }
        return null;
    }

    public void callPending(Container container) {
        for(Player p : container.getPendingPlayerConnections()) {
            if (p.getCurrentServer().isEmpty() || !p.getCurrentServer().get().getServerInfo().getName().equals(container.getServerName())) {
                connectPlayer(p, container, false);
            }
        }
    }

    public void logGroupInfo(CommandSource source, Group group) {
        Nebula.util.sendMessage(source, "Group Information");
        Nebula.util.sendMessage(source, "Name:      " + group.getName());
        Nebula.util.sendMessage(source, "Prefix:    " + group.getPrefix());
        Nebula.util.sendMessage(source, "Level:     " + group.getLevel());
    }

    public static String getPlayerNameFromUUID(String uuid) {
        try {
            String uuidStr = uuid.replace("-", "");
            String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuidStr;
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Velocity Plugin");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            if (connection.getResponseCode() == 200) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();
                return json.get("name").getAsString();
            } else if (connection.getResponseCode() == 204 || connection.getResponseCode() == 404) {
                Nebula.util.log("Player not found for UUID: {}", uuid);
            } else {
                Nebula.util.log("Failed to fetch player name. HTTP Response: {}", connection.getResponseCode());
            }
        } catch (Exception e) {
            Nebula.util.log(e.getMessage());
        }
        return "Null";
    }

    public int getPlayerCount(Object backendServer) {
        if(backendServer instanceof RegisteredServer registeredServer && backendServer != null) {
            return registeredServer.getPlayersConnected().size();
        }
        if(backendServer instanceof Container backend && backendServer != null) {
            return Nebula.server.getServer(backend.getServerName()).get().getPlayersConnected().size();
        }
        return 0;
    }

    public void sendMessage(CommandSource source, String message) {
        source = source != null ? source : Nebula.server.getConsoleCommandSource();
        if (source == Nebula.server.getConsoleCommandSource()) {
            source.sendMessage(mm.deserialize(message));
        } else if(source instanceof Player player){
            player.sendMessage(mm.deserialize(message));
            Nebula.server.getConsoleCommandSource().sendMessage(mm.deserialize(player.getUsername() + " --> " + message));
        }
    }

    public void sendMemberMessage(Party party, String message) {
        for(Player member : party.getMembers()) {
            sendMessage(member, message);
        }
    }

    public void log(String message, Object... args) {
        for (Object arg : args) {
            message = message.replaceFirst("\\{}", arg != null ? arg.toString() : "");
        }
        Nebula.server.getConsoleCommandSource().sendMessage(mm.deserialize(message));
    }
}