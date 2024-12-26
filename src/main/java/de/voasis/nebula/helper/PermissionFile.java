package de.voasis.nebula.helper;

import de.voasis.nebula.Nebula;
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

    public void addPermissionToGroup(String groupName, String permission) {
        try {
            ConfigurationNode permissionsNode = rootNode.node("groups", groupName, "permissions");
            List<String> permissions = permissionsNode.getList(String.class, new ArrayList<>());
            if (!permissions.contains(permission)) {
                permissions.add(permission);
                permissionsNode.set(permissions);
                saveConfig();
                Nebula.util.log("Added permission \"" + permission + "\" to group \"" + groupName + "\" in config.");
            }
        } catch (IOException e) {
            Nebula.util.log("Failed to add permission to group: " + e.getMessage());
        }
    }

    public void removePermissionFromGroup(String groupName, String permission) {
        try {
            ConfigurationNode permissionsNode = rootNode.node("groups", groupName, "permissions");
            List<String> permissions = permissionsNode.getList(String.class, new ArrayList<>());
            if (permissions.contains(permission)) {
                permissions.remove(permission);
                permissionsNode.set(permissions);
                saveConfig();
                Nebula.util.log("Removed permission \"" + permission + "\" from group \"" + groupName + "\" in config.");
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

    public void addMemberToGroup(String groupName, String playerUUID) {
        try {
            ConfigurationNode membersNode = rootNode.node("groups", groupName, "members");
            List<String> members = membersNode.getList(String.class, new ArrayList<>());
            if (!members.contains(playerUUID)) {
                members.add(playerUUID);
                membersNode.set(members);
                saveConfig();
            }
        } catch (IOException e) {
            Nebula.util.log("Failed to add player to group members: " + e.getMessage());
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