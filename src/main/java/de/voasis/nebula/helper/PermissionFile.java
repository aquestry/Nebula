package de.voasis.nebula.helper;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                        System.out.println("Default perms.conf copied from resources.");
                    } else {
                        System.out.println("Could not find perms.conf in resources.");
                    }
                }
            }
            loader = HoconConfigurationLoader.builder()
                    .file(configFilePath.toFile())
                    .build();
            rootNode = loader.load();
        } catch (IOException e) {
            System.out.println("Failed to initialize configuration: " + e.getMessage());
        }
    }

    public String getDefaultGroupName() {
        try {
            return rootNode.node("default-group").getString("default");
        } catch (Exception e) {
            System.out.println("Failed to fetch default group name: " + e.getMessage());
            return "default";
        }
    }

    public List<String> getGroupMembers(String groupName) {
        try {
            ConfigurationNode membersNode = rootNode.node("groups", groupName, "members");
            return membersNode.getList(String.class, Collections.emptyList());
        } catch (IOException e) {
            System.out.println("Failed to fetch group members for group: " + groupName);
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
                System.out.println("Added player " + playerUUID + " to group " + groupName + " members list.");
            }
        } catch (IOException e) {
            System.out.println("Failed to add player to group members: " + e.getMessage());
        }
    }

    public ConfigurationNode getGroupNode(String groupName) {
        return rootNode.node("groups", groupName);
    }

    public void saveConfig() {
        try {
            loader.save(rootNode);
            System.out.println("Configuration saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save configuration: " + e.getMessage());
        }
    }

    public ConfigurationNode getRootNode() {
        return rootNode;
    }
}