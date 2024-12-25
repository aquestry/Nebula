package de.voasis.nebula.helper;

import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.map.Group;
import java.util.*;
import java.util.stream.Collectors;

public class PermissionManager {

    private final List<Group> groups = new ArrayList<>();
    private final Map<UUID, Group> playerGroups = new HashMap<>();

    public PermissionManager() {
        loadGroupsFromConfig();
    }

    private void loadGroupsFromConfig() {
        PermissionFile permissionFile = Nebula.permissionFile;
        for (Group group : groups) {
            List<String> members = permissionFile.getGroupMembers(group.getName());
            for (String memberUUID : members) {
                try {
                    UUID uuid = UUID.fromString(memberUUID);
                    playerGroups.put(uuid, group);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid UUID in group " + group.getName() + ": " + memberUUID);
                }
            }
        }
    }

    public Group getGroup(Player player) {
        UUID playerUUID = player.getUniqueId();
        List<Group> playerGroupsInConfig = groups.stream()
                .filter(group -> Nebula.permissionFile.getGroupMembers(group.getName()).contains(playerUUID.toString()))
                .collect(Collectors.toList());
        Group highestLevelGroup = playerGroupsInConfig.stream()
                .max(Comparator.comparingInt(Group::getLevel))
                .orElse(null);
        if (highestLevelGroup != null) {
            playerGroups.put(playerUUID, highestLevelGroup);
            System.out.println("Player " + player.getUsername() + " assigned to group: " + highestLevelGroup.getName());
            return highestLevelGroup;
        }
        String defaultGroupName = Nebula.permissionFile.getDefaultGroupName();
        Group defaultGroup = getGroupByName(defaultGroupName);
        if (defaultGroup != null) {
            playerGroups.put(playerUUID, defaultGroup);
            System.out.println("Player " + player.getUsername() + " assigned to default group: " + defaultGroupName);
            Nebula.permissionFile.addMemberToGroup(defaultGroupName, playerUUID.toString());
        }
        return defaultGroup;
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
        if (!name.equalsIgnoreCase(Nebula.permissionFile.getDefaultGroupName())) {
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