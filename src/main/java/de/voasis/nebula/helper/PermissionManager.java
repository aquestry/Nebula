package de.voasis.nebula.helper;

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
        ConfigurationNode groupNode = Nebula.permissionFile.getGroupNode(groupName);
        List<String> permissions = new ArrayList<>();
        try {
            permissions = groupNode.node("permissions").getList(String.class, Collections.emptyList());
        } catch (Exception e) {
            System.out.println("Failed to fetch permissions for group \"" + groupName + "\": " + e.getMessage());
        }
        StringBuilder log = new StringBuilder();
        log.append("╔════════════════════════════════════════╗\n");
        log.append("║ Group Information                       ║\n");
        log.append("╠════════════════════════════════════════╣\n");
        log.append("║ Name:      ").append(group.getName()).append("\n");
        log.append("║ Prefix:    ").append(group.getPrefix()).append("\n");
        log.append("║ Level:     ").append(group.getLevel()).append("\n");
        log.append("║ Members:   ").append(members.size()).append("\n");
        for (String member : members) {
            log.append("║   - ").append(member).append("\n");
        }
        log.append("║ Permissions: ").append(permissions.size()).append("\n");
        for (String permission : permissions) {
            log.append("║   - ").append(permission).append("\n");
        }
        log.append("╚════════════════════════════════════════╝");
        System.out.println(log);
    }

    private void loadGroupsFromConfig() {
        PermissionFile permissionFile = Nebula.permissionFile;
        groups.clear();
        for (String groupName : permissionFile.getGroupNames()) {
            ConfigurationNode groupNode = permissionFile.getGroupNode(groupName);
            if (groupNode != null) {
                String prefix = groupNode.node("prefix").getString("");
                int level = groupNode.node("level").getInt(0);
                Group group = new Group(groupName, prefix, level);
                groups.add(group);
                logGroupInfo(group);
                List<String> members = permissionFile.getGroupMembers(groupName);
                for (String memberUUID : members) {
                    try {
                        UUID uuid = UUID.fromString(memberUUID);
                        playerGroups.put(uuid, group);
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
        if (defaultGroupName == null || defaultGroupName.isEmpty()) {
            Group fallbackGroup = new Group("fallback", "<gray>[Fallback]<white>", 0);
            playerGroups.put(playerUUID, fallbackGroup);
            return fallbackGroup;
        }
        Group defaultGroup = getGroupByName(defaultGroupName);
        if (defaultGroup != null) {
            playerGroups.put(playerUUID, defaultGroup);
            Nebula.permissionFile.addMemberToGroup(defaultGroupName, playerUUID.toString());
            return defaultGroup;
        }
        Group fallbackGroup = new Group("fallback", "<dark_gray>[<gray>Fallback<dark_gray>] <white>", 0);
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