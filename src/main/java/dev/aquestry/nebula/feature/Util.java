package dev.aquestry.nebula.feature;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.data.Messages;
import dev.aquestry.nebula.model.Container;
import dev.aquestry.nebula.model.Group;
import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.model.Party;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    public String calculateHMAC(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(Config.HMACSecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC", e);
        }
    }

    public void checkLobbys(boolean online) {
        int lobbys = 0;
        for(Container container : Config.containerMap) {
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
        for (Container server : Config.containerMap) {
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
        Nebula.util.sendMessage(source, "Members:     ");
        for(String member : group.getMembers()) {
            Nebula.util.sendMessage(source, member);
        }
        Nebula.util.sendMessage(source, "Permissions:     ");
        for(String permission : group.getPermissions()) {
            Nebula.util.sendMessage(source, permission);
        }
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

    public String getUUID(String name) {
        String uuid;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream()));
            uuid = (((JsonObject)new JsonParser().parse(in)).get("id")).toString().replaceAll("\"", "");
            uuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            in.close();
        } catch (Exception e) {
            uuid = "ERROR";
        }
        return uuid;
    }

    public void sendInfotoBackend(Player player) {
        Group group = Nebula.permissionManager.getGroup(player.getUniqueId().toString());
        String info = player.getUsername() + ":" + group.getName() + "#" + group.getLevel() + "#" + group.getPrefix();
        player.getCurrentServer().ifPresent(serverConnection -> serverConnection.getServer().sendPluginMessage(Nebula.channelMain, info.getBytes()));
    }
}