package de.voasis.nebula.helper;

import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.map.Group;
import org.spongepowered.configurate.ConfigurationNode;
import java.io.IOException;
import java.util.*;

public class PermissionManager implements PermissionProvider {

    private final List<Group> groups = new ArrayList<>();
    private final Map<UUID, Group> playerGroups = new HashMap<>();

    public PermissionManager() {
        loadGroupsFromConfig();
    }

    public boolean hasPermission(Player player, String permission) {
        return getGroup(player).hasPermission(permission);
    }

    @Override
    public PermissionFunction createFunction(PermissionSubject subject) {
        return permission -> {
            if (subject instanceof Player) {
                return Tristate.fromBoolean(hasPermission((Player) subject, permission));
            }
            return Tristate.FALSE;
        };
    }

    private void loadGroupsFromConfig() {
        try {
            ConfigurationNode rootNode = Nebula.permissionFile.getRootNode();
            String defaultGroupName = rootNode.node("default-group").getString("default");
            ConfigurationNode groupsNode = rootNode.node("groups");
            for (Map.Entry<Object, ? extends ConfigurationNode> entry : groupsNode.childrenMap().entrySet()) {
                String groupName = entry.getKey().toString();
                ConfigurationNode groupNode = entry.getValue();
                String prefix = groupNode.node("prefix").getString();
                int level = groupNode.node("level").getInt(1);
                List<String> permissions = groupNode.node("permissions").getList(String.class, Collections.emptyList());
                Group group = new Group(groupName, prefix, level);
                permissions.forEach(group::addPermission);
                groups.add(group);
                System.out.println("Loaded group: " + groupName + " with " + permissions.size() + " permissions.");
            }

        } catch (IOException e) {
            System.out.println("Failed to load groups from config: " + e.getMessage());
        }
    }

    public Group getGroup(Player player) {
        Group group = playerGroups.get(player.getUniqueId());
        if (group == null) {
            group = getGroupByName("default");
            if (group != null) {
                playerGroups.put(player.getUniqueId(), group);
                System.out.println("Player " + player.getUsername() + " assigned to default group.");
                try {
                    ConfigurationNode rootNode = Nebula.permissionFile.getRootNode();
                    ConfigurationNode defaultGroupNode = rootNode.node("groups", "default", "members");
                    List<String> members = defaultGroupNode.getList(String.class, new ArrayList<>());
                    if (!members.contains(player.getUniqueId().toString())) {
                        members.add(player.getUniqueId().toString());
                        defaultGroupNode.set(members);
                        Nebula.permissionFile.saveConfig();
                        System.out.println("Player " + player.getUsername() + " added to default group members in config.");
                    }
                } catch (IOException e) {
                    System.out.println("Failed to update default group members: " + e.getMessage());
                }
            }
        }
        return group;
    }

    public void assignGroup(Player player, String groupName) {
        Group group = getGroupByName(groupName);
        if (group != null) {
            playerGroups.put(player.getUniqueId(), group);
            System.out.println("Player " + player.getUsername() + " assigned to group: " + groupName);
        } else {
            System.out.println("Group not found: " + groupName);
        }
    }

    public void createGroup(String name, String prefix, int level) {
        if (getGroupByName(name) == null) {
            groups.add(new Group(name, prefix, level));
            System.out.println("Group created: " + name);
            saveGroupsToConfig();
        } else {
            System.out.println("Group already exists: " + name);
        }
    }

    public void deleteGroup(String name) {
        if (!name.equalsIgnoreCase("default")) {
            groups.removeIf(group -> group.getName().equalsIgnoreCase(name));
            System.out.println("Group removed: " + name);
            saveGroupsToConfig();
        } else {
            System.out.println("Cannot remove group: " + name);
        }
    }

    public void listGroups() {
        groups.forEach(group -> {
            System.out.println("Group: " + group.getName());
            System.out.println("  Prefix: " + group.getPrefix());
            System.out.println("  Level: " + group.getLevel());
        });
    }

    public String getGroupInfo(Player player) {
        Group group = getGroup(player);
        return player.getUsername() + ":" + group.getName() + "#" + group.getLevel() + "#" + group.getPrefix();
    }

    public void saveGroupsToConfig() {
        try {
            ConfigurationNode rootNode = Nebula.permissionFile.getRootNode();
            ConfigurationNode groupsNode = rootNode.node("groups");
            groupsNode.childrenMap().clear();
            for (Group group : groups) {
                ConfigurationNode groupNode = groupsNode.node(group.getName());
                groupNode.node("prefix").set(group.getPrefix());
                groupNode.node("level").set(group.getLevel());
            }
            Nebula.permissionFile.saveConfig();
            System.out.println("Groups saved to configuration file.");
        } catch (Exception e) {
            System.out.println("Failed to save groups: " + e.getMessage());
        }
    }

    private Group getGroupByName(String name) {
        return groups.stream()
                .filter(group -> group.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}