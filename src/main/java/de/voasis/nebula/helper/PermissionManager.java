package de.voasis.nebula.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Group;
import org.spongepowered.configurate.ConfigurationNode;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class PermissionManager implements PermissionProvider {

    private final List<Group> groups = new ArrayList<>();
    private final Map<UUID, Group> playerGroups = new HashMap<>();

    public PermissionManager() {
        loadGroupsFromConfig();
        Data.defaultGroupName = Nebula.permissionFile.getDefaultGroupName();
    }

    public boolean hasPermission(Player player, String permission) {
        Group group = getGroup(player);
        return group != null && group.hasPermission(permission);
    }

    @Subscribe
    public PermissionFunction createFunction(PermissionSubject subject) {
        return permission -> Tristate.fromBoolean(
                subject instanceof Player && hasPermission((Player) subject, permission)
        );
    }

    public void logGroupInfo(CommandSource source, Group group) {
        String groupName = group.getName();
        List<String> members = Nebula.permissionFile.getGroupMembers(groupName);
        List<String> permissions = group.getPermissions();
        ConfigurationNode groupNode = Nebula.permissionFile.getGroupNode(groupName);
        Nebula.util.sendMessage(source, "Group Information");
        Nebula.util.sendMessage(source, "Name:      " + groupName);
        Nebula.util.sendMessage(source, "Prefix:    " + group.getPrefix());
        Nebula.util.sendMessage(source, "Level:     " + group.getLevel());
        Nebula.util.sendMessage(source, "Members:   " + members.size());
        for (String member : members) {
            Nebula.util.sendMessage(source, member + " : " + getPlayerNameFromUUID(member));
        }
        Nebula.util.sendMessage(source, "Permissions:");
        for (String perm : permissions) {
            Nebula.util.sendMessage(source, perm);
        }
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

    private void loadGroupsFromConfig() {
        PermissionFile permissionFile = Nebula.permissionFile;
        groups.clear();
        playerGroups.clear();
        for (String groupName : permissionFile.getGroupNames()) {
            ConfigurationNode groupNode = permissionFile.getGroupNode(groupName);
            if (groupNode != null) {
                String prefix = groupNode.node("prefix").getString("");
                int level = groupNode.node("level").getInt(0);
                List<String> permissions = new ArrayList<>();
                try {
                    permissions = groupNode.node("permissions").getList(String.class, new ArrayList<>());
                } catch (Exception e) {
                    Nebula.util.log("Failed to load permissions for group \"" + groupName + "\": " + e.getMessage());
                }
                Group group = new Group(groupName, prefix, level);
                permissions.forEach(group::addPermission);
                groups.add(group);
                logGroupInfo(Nebula.server.getConsoleCommandSource(), group);
                List<String> members = permissionFile.getGroupMembers(groupName);
                for (String memberUUID : members) {
                    try {
                        UUID uuid = UUID.fromString(memberUUID);
                        if (!playerGroups.containsKey(uuid)) {
                            playerGroups.put(uuid, group);
                        }
                    } catch (IllegalArgumentException e) {
                        Nebula.util.log("Invalid UUID in group " + groupName + ": " + memberUUID);
                    }
                }
            }
        }
    }


    public Group getGroup(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (playerGroups.containsKey(playerUUID)) {
            return playerGroups.get(playerUUID);
        }
        Group highestLevelGroup = groups.stream()
                .filter(group -> Nebula.permissionFile.getGroupMembers(group.getName()).contains(playerUUID.toString()))
                .max(Comparator.comparingInt(Group::getLevel))
                .orElse(null);
        if (highestLevelGroup != null) {
            playerGroups.put(playerUUID, highestLevelGroup);
            return highestLevelGroup;
        }
        String defaultGroupName = Data.defaultGroupName;
        Group fallbackGroup = new Group("fallback", "<dark_gray>[<gray>Fallback<dark_gray>] <white>", 0);
        Group defaultGroup = getGroupByName(defaultGroupName);
        if (defaultGroup != null) {
            playerGroups.put(playerUUID, defaultGroup);
            Nebula.permissionFile.addMemberToGroup(defaultGroup, player);
            return defaultGroup;
        }
        playerGroups.put(playerUUID, fallbackGroup);
        return fallbackGroup;
    }

    public void assignGroup(Player player, Group group) {
        if (group != null) {
            playerGroups.remove(player.getUniqueId());
            playerGroups.put(player.getUniqueId(), group);
            Nebula.permissionFile.removeMemberFromGroup(getGroup(player), player);
            Nebula.permissionFile.addMemberToGroup(group, player);
        }
    }

    public void createGroup(String name, String prefix, int level) {
        if (getGroupByName(name) == null) {
            groups.add(new Group(name, prefix, level));
            Nebula.permissionFile.saveConfig();
        }
    }

    public void deleteGroup(String name) {
        if (!name.equalsIgnoreCase(Data.defaultGroupName)) {
            groups.removeIf(group -> group.getName().equalsIgnoreCase(name));
            Nebula.permissionFile.saveConfig();
        }
    }

    public String getGroupInfo(Player player) {
        Group group = getGroup(player);
        return player.getUsername() + ":" + group.getName() + "#" + group.getLevel() + "#" + group.getPrefix();
    }

    public Group getGroupByName(String name) {
        return groups.stream()
                .filter(group -> group.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}