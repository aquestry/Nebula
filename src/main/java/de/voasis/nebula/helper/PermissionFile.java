package de.voasis.nebula.helper;

import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.map.Group;
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

    public PermissionFile(Path dataDirectory) {
        this.configFilePath = dataDirectory.resolve("perms.conf");
        initializeConfig();
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

    public String getDefaultGroupName() {
        try {
            return rootNode.node("default-group").getString("default");
        } catch (Exception e) {
            return "default";
        }
    }

    public void addPermissionToGroup(Group group, String permission) {
        try {
            ConfigurationNode permissionsNode = rootNode.node("groups", group.getName(), "permissions");
            List<String> permissions = new ArrayList<>(permissionsNode.getList(String.class, new ArrayList<>()));
            if (!permissions.contains(permission)) {
                permissions.add(permission);
                permissionsNode.set(permissions);
                group.addPermission(permission);
                saveConfig();
                Nebula.util.log("Added permission \"" + permission + "\" to group \"" + group.getName() + "\" in config.");
            } else {
                Nebula.util.log("Permission \"" + permission + "\" already exists in group \"" + group.getName() + "\".");
            }
        } catch (IOException e) {
            Nebula.util.log("Failed to add permission to group: " + e.getMessage());
        }
    }

    public void removePermissionFromGroup(Group group, String permission) {
        try {
            ConfigurationNode permissionsNode = rootNode.node("groups", group.getName(), "permissions");
            List<String> permissions = new ArrayList<>(permissionsNode.getList(String.class, new ArrayList<>()));
            if (permissions.contains(permission)) {
                permissions.remove(permission);
                permissionsNode.set(permissions);
                group.removePermission(permission);
                saveConfig();
                Nebula.util.log("Removed permission \"" + permission + "\" from group \"" + group.getName() + "\" in config.");
            } else {
                Nebula.util.log("Permission \"" + permission + "\" not found in group \"" + group.getName() + "\".");
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

    public void addMemberToGroup(Group group, Player player) {
        try {
            ConfigurationNode membersNode = rootNode.node("groups", group.getName(), "members");
            List<String> members = new ArrayList<>(membersNode.getList(String.class, new ArrayList<>()));
            String playerUUID = player.getUniqueId().toString();
            if (!members.contains(playerUUID)) {
                members.add(playerUUID);
                membersNode.set(members);
                saveConfig();
                Nebula.util.log("Added player \"" + player.getUsername() + "\" (UUID: " + playerUUID + ") to group \"" + group.getName() + "\" in config.");
            } else {
                Nebula.util.log("Player \"" + player.getUsername() + "\" (UUID: " + playerUUID + ") is already in group \"" + group.getName() + "\".");
            }
        } catch (IOException e) {
            Nebula.util.log("Failed to add player to group members: " + e.getMessage());
        }
    }

    public void removeMemberFromGroup(Group group, Player player) {
        try {
            ConfigurationNode membersNode = rootNode.node("groups", group.getName(), "members");
            List<String> members = new ArrayList<>(membersNode.getList(String.class, new ArrayList<>()));
            String playerUUID = player.getUniqueId().toString();
            if (members.contains(playerUUID)) {
                members.remove(playerUUID);
                membersNode.set(members);
                saveConfig();
                Nebula.util.log("Removed player \"" + player.getUsername() + "\" (UUID: " + playerUUID + ") from group \"" + group.getName() + "\" in config.");
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