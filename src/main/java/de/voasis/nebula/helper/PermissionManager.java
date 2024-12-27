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
    private HashMap<Player, Group> cachedGroups = new HashMap<>();

    public PermissionManager() {
        loadGroupsFromConfig();
        Data.defaultGroupName = Nebula.permissionFile.getDefaultGroupName();
    }

    public boolean hasPermission(Player player, String permission) {
        for(Group group : groups) {
            if(Nebula.permissionFile.getGroupMembers(group.getName()).contains(player.getUniqueId().toString())) {
                return group.hasPermission(permission);
            }
        }
        return false;
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
            }
        }
    }

    public Group getGroup(Player player) {
        UUID playerUUID = player.getUniqueId();
        if(cachedGroups.containsKey(player)) {
            return cachedGroups.get(player);
        }
        Group highestLevelGroup = null;
        List<Group> sortedGroups = new ArrayList<>(groups);
        sortedGroups.sort((g1, g2) -> Integer.compare(g2.getLevel(), g1.getLevel()));
        for (Group group : sortedGroups) {
            List<String> members = Nebula.permissionFile.getGroupMembers(group.getName());
            if (members.contains(playerUUID.toString())) {
                if (highestLevelGroup == null) {
                    highestLevelGroup = group;
                } else if (group.getLevel() > highestLevelGroup.getLevel()) {
                    Nebula.util.log("Group {} has a higher level ({}) than current highest-level group {} ({})",
                            group.getName(), group.getLevel(), highestLevelGroup.getName(), highestLevelGroup.getLevel());
                    highestLevelGroup = group;
                }
            }
        }
        if (highestLevelGroup != null) {
            Nebula.util.log("Player: {}, UUID: {}, Group: {}", player.getUsername(), playerUUID, highestLevelGroup.getName());
            cachedGroups.put(player, highestLevelGroup);
            return highestLevelGroup;
        }
        Group defaultGroup = getGroupByName(Data.defaultGroupName);
        if (defaultGroup != null) {
            Nebula.permissionFile.addMemberToGroup(defaultGroup, player, null);
            Nebula.util.log("No specific group found. Assigning default group: {} to player {}", defaultGroup.getName(), playerUUID);
            cachedGroups.put(player, defaultGroup);
            return defaultGroup;
        }
        Nebula.util.log("No default group found. Assigning fallback group to player: {}", playerUUID);
        return new Group("fallback", "<dark_gray>[<gray>Fallback<dark_gray>] <white>", 0);
    }

    public void assignGroup(Player player, Group group) {
        if (group != null) {
            cachedGroups.remove(player);
            Nebula.permissionFile.removeMemberFromGroup(getGroup(player), player, null);
            Nebula.permissionFile.addMemberToGroup(group, player, null);
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