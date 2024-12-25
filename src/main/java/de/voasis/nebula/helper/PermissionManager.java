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

    private void loadGroupsFromConfig() {
        PermissionFile permissionFile = Nebula.permissionFile;
        groups.clear();
        for (String groupName : permissionFile.getGroupNames()) {
            Group group = getGroupByName(groupName);
            if (group != null) {
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
            System.out.println("Player " + player.getUsername() + " assigned to group: " + highestLevelGroup.getName());
            return highestLevelGroup;
        }
        String defaultGroupName = Data.defaultGroupName;
        if (defaultGroupName == null || defaultGroupName.isEmpty()) {
            System.out.println("Default group name is not defined in config. Assigning fallback group.");
            Group fallbackGroup = new Group("fallback", "<gray>[Fallback]<white>", 0);
            playerGroups.put(playerUUID, fallbackGroup);
            return fallbackGroup;
        }
        Group defaultGroup = getGroupByName(defaultGroupName);
        if (defaultGroup != null) {
            playerGroups.put(playerUUID, defaultGroup);
            System.out.println("Player " + player.getUsername() + " assigned to default group: " + defaultGroupName);
            Nebula.permissionFile.addMemberToGroup(defaultGroupName, playerUUID.toString());
            return defaultGroup;
        }
        System.out.println("Default group \"" + defaultGroupName + "\" is not found in loaded groups. Assigning fallback group.");
        Group fallbackGroup = new Group("fallback", "<dark_gray>[<gray>Fallback<dark_gray>] <white>", 0);
        playerGroups.put(playerUUID, fallbackGroup);
        return fallbackGroup;
    }

    public void assignGroup(Player player, String groupName) {
        Group group = getGroupByName(groupName);
        if (group != null) {
            playerGroups.put(player.getUniqueId(), group);
            Nebula.permissionFile.addMemberToGroup(groupName, player.getUniqueId().toString());
            System.out.println("Player " + player.getUsername() + " assigned to group: " + groupName);
        } else {
            System.out.println("Group not found: " + groupName);
        }
    }

    public void createGroup(String name, String prefix, int level) {
        if (getGroupByName(name) == null) {
            groups.add(new Group(name, prefix, level));
            Nebula.permissionFile.saveConfig();
            System.out.println("Group created: " + name);
        } else {
            System.out.println("Group already exists: " + name);
        }
    }

    public void deleteGroup(String name) {
        if (!name.equalsIgnoreCase(Data.defaultGroupName)) {
            groups.removeIf(group -> group.getName().equalsIgnoreCase(name));
            Nebula.permissionFile.saveConfig();
            System.out.println("Group removed: " + name);
        } else {
            System.out.println("Cannot remove default group: " + name);
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