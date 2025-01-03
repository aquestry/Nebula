package de.voasis.nebula.manager;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.data.Messages;
import de.voasis.nebula.model.Group;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PermissionFile {

    private final Path configFilePath;
    private ConfigurationLoader<?> loader;
    private ConfigurationNode rootNode;
    public final List<Group> groups = new ArrayList<>();

    public PermissionFile(Path dataDirectory) {
        this.configFilePath = dataDirectory.resolve("perms.conf");
        initializeConfig();
        loadGroupsFromConfig();
        Config.defaultGroupName = getDefaultGroupName();
    }

    private void initializeConfig() {
        try {
            if (!Files.exists(configFilePath)) {
                Files.createDirectories(configFilePath.getParent());
                try (InputStream resource = getClass().getClassLoader().getResourceAsStream("perms.conf")) {
                    if (resource != null) {
                        Files.copy(resource, configFilePath);
                    }
                }
            }
            loader = HoconConfigurationLoader.builder()
                    .file(configFilePath.toFile())
                    .build();
            rootNode = loader.load();
        } catch (IOException e) {
            Nebula.util.log("Failed to initialize configuration: " + e.getMessage());
        }
    }

    public void loadGroupsFromConfig() {
        for (String groupName : getGroupNames()) {
            ConfigurationNode groupNode = getGroupNode(groupName);
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
                Nebula.util.logGroupInfo(Nebula.server.getConsoleCommandSource(), group);
            }
        }
    }

    public String getDefaultGroupName() {
        try {
            return rootNode.node("default-group").getString("default");
        } catch (Exception e) {
            return "default";
        }
    }

    public void addPermissionToGroup(Group group, String permission, CommandSource source) {
        try {
            ConfigurationNode permissionsNode = rootNode.node("groups", group.getName(), "permissions");
            List<String> permissions = new ArrayList<>(permissionsNode.getList(String.class, new ArrayList<>()));
            if (!permissions.contains(permission)) {
                permissions.add(permission);
                List<String> sanitizedPermissions = permissions.stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());
                permissionsNode.setList(String.class, sanitizedPermissions);
                group.addPermission(permission);
                saveConfig();
                Nebula.util.sendMessage(source, Messages.GROUP_PERMISSION_ADD_SUCCESS.replace("<permission>", permission).replace("<group>", group.getName()));
            } else {
                Nebula.util.sendMessage(source, Messages.GROUP_PERMISSION_ALREADY_EXISTS.replace("<permission>", permission).replace("<group>", group.getName()));
            }
        } catch (IOException e) {
            Nebula.util.log("Failed to add permission to group: " + e.getMessage());
        }
    }

    public void removePermissionFromGroup(Group group, String permission, CommandSource source) {
        try {
            ConfigurationNode permissionsNode = rootNode.node("groups", group.getName(), "permissions");
            List<String> permissions = new ArrayList<>(permissionsNode.getList(String.class, new ArrayList<>()));
            if (permissions.contains(permission)) {
                permissions.remove(permission);
                permissionsNode.set(permissions);
                group.removePermission(permission);
                saveConfig();
                Nebula.util.sendMessage(source, Messages.GROUP_PERMISSION_REMOVE_SUCCESS.replace("<permission>", permission).replace("<group>", group.getName()));
            } else {
                Nebula.util.sendMessage(source, Messages.GROUP_PERMISSION_REMOVE_NOT_FOUND.replace("<permission>", permission).replace("<group>", group.getName()));
            }
        } catch (IOException e) {
            Nebula.util.log("Failed to remove permission from group: " + e.getMessage());
        }
    }

    public List<String> getGroupMembers(String groupName) {
        try {
            ConfigurationNode membersNode = rootNode.node("groups", groupName, "members");
            return membersNode.getList(String.class, Collections.emptyList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public void addMemberToGroup(Group group, Player player, CommandSource source) {
        try {
            ConfigurationNode membersNode = rootNode.node("groups", group.getName(), "members");
            List<String> members = new ArrayList<>(membersNode.getList(String.class, new ArrayList<>()));
            String playerUUID = player.getUniqueId().toString();
            if (!members.contains(playerUUID)) {
                members.add(playerUUID);
                membersNode.set(members);
                saveConfig();
            } else {
                Nebula.util.log("Player \"" + player.getUsername() + "\" (UUID: " + playerUUID + ") is already in group \"" + group.getName() + "\".");
            }
        } catch (IOException e) {
            Nebula.util.log("Failed to add player to group members: " + e.getMessage());
        }
    }

    public void removeMemberFromGroup(Group group, Player player, CommandSource source) {
        try {
            ConfigurationNode membersNode = rootNode.node("groups", group.getName(), "members");
            List<String> members = new ArrayList<>(membersNode.getList(String.class, new ArrayList<>()));
            String playerUUID = player.getUniqueId().toString();
            if (members.contains(playerUUID)) {
                members.remove(playerUUID);
                membersNode.set(members);
                saveConfig();
            } else {
                Nebula.util.log("Player \"" + player.getUsername() + "\" (UUID: " + playerUUID + ") is not a member of group \"" + group.getName() + "\".");
            }
        } catch (IOException e) {
            Nebula.util.log("Failed to remove player from group members: " + e.getMessage());
        }
    }

    public List<String> getGroupNames() {
        try {
            ConfigurationNode groupsNode = rootNode.node("groups");
            return groupsNode.childrenMap().keySet().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public ConfigurationNode getGroupNode(String groupName) {
        return rootNode.node("groups", groupName);
    }

    public void saveConfig() {
        try {
            loader.save(rootNode);
        } catch (IOException e) {
            Nebula.util.log("Failed to save configuration: " + e.getMessage());
        }
    }

    public ConfigurationNode getRootNode() {
        return rootNode;
    }
}