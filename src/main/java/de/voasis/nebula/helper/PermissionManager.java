package de.voasis.nebula.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    public void logGroupInfo(Group group) {
        String groupName = group.getName();
        List<String> members = Nebula.permissionFile.getGroupMembers(groupName);
        List<String> permissions = group.getPermissions();
        ConfigurationNode groupNode = Nebula.permissionFile.getGroupNode(groupName);
        StringBuilder log = new StringBuilder();
        log.append("Group Information\n");
        log.append("Name:      ").append(group.getName()).append("\n");
        log.append("Prefix:    ").append(group.getPrefix()).append("\n");
        log.append("Level:     ").append(group.getLevel()).append("\n");
        log.append("Members:   ").append(members.size()).append("\n");
        for (String member : members) {
            log.append(member).append(": ").append(getPlayerNameFromUUID(member)).append("\n");
        }
        log.append("Permissions:   ").append(permissions.size()).append("\n");
        for (String perm : permissions) {
            log.append(perm).append("\n");
        }
        System.out.println(log);
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
                System.out.println("Player not found for UUID: " + uuid);
            } else {
                System.out.println("Failed to fetch player name. HTTP Response: " + connection.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                    System.out.println("Failed to load permissions for group \"" + groupName + "\": " + e.getMessage());
                }
                Group group = new Group(groupName, prefix, level);
                permissions.forEach(group::addPermission);
                groups.add(group);
                logGroupInfo(group);
                List<String> members = permissionFile.getGroupMembers(groupName);
                for (String memberUUID : members) {
                    try {
                        UUID uuid = UUID.fromString(memberUUID);
                        if (!playerGroups.containsKey(uuid)) {
                            playerGroups.put(uuid, group);
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid UUID in group " + groupName + ": " + memberUUID);
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
        if (defaultGroupName == null) {
            playerGroups.put(playerUUID, fallbackGroup);
            return fallbackGroup;
        }
        Group defaultGroup = getGroupByName(defaultGroupName);
        if (defaultGroup != null) {
            playerGroups.put(playerUUID, defaultGroup);
            Nebula.permissionFile.addMemberToGroup(defaultGroupName, playerUUID.toString());
            return defaultGroup;
        }
        playerGroups.put(playerUUID, fallbackGroup);
        return fallbackGroup;
    }

    public void assignGroup(Player player, String groupName) {
        Group group = getGroupByName(groupName);
        if (group != null) {
            playerGroups.put(player.getUniqueId(), group);
            Nebula.permissionFile.addMemberToGroup(groupName, player.getUniqueId().toString());
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