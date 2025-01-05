package de.voasis.nebula.manager;

import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Group;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class PermissionFile {

    private final Path configFilePath;
    private ConfigurationLoader<?> loader;
    private ConfigurationNode rootNode;
    public final List<Group> runtimeGroups = new ArrayList<>();

    public PermissionFile(Path dataDirectory) {
        this.configFilePath = dataDirectory.resolve("perms.conf");
        reloadGroups();
        Config.defaultGroupName = rootNode.node("default-group").getString("default");
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
            Nebula.util.log("Failed to initialize perms: {}.", e.getMessage());
        }
    }

    public void reloadGroups() {
        runtimeGroups.clear();
        initializeConfig();
        for (String groupName : getGroupNames()) {
            ConfigurationNode groupNode = getGroupNode(groupName);
            if (groupNode != null) {
                String prefix = groupNode.node("prefix").getString("");
                int level = groupNode.node("level").getInt(0);
                List<String> permissions = new ArrayList<>();
                List<String> memberUUIDs = new ArrayList<>();
                try {
                    permissions = groupNode.node("permissions").getList(String.class, new ArrayList<>());
                    memberUUIDs = groupNode.node("members").getList(String.class, new ArrayList<>());
                } catch (Exception ignored) {}
                Group group = new Group(groupName, prefix, level);
                permissions.forEach(group::addPermission);
                memberUUIDs.forEach(group::addMember);
                runtimeGroups.add(group);
            }
        }
        for(Player player : Nebula.server.getAllPlayers()) {
            Nebula.permissionManager.sendInfotoBackend(player);
        }
    }

    public void saveGroup(Group group) {
        try {
            ConfigurationNode groupNode = rootNode.node("groups", group.getName());
            groupNode.node("prefix").set(group.getPrefix());
            groupNode.node("level").set(group.getLevel());
            groupNode.node("permissions").set(group.getPermissions());
            groupNode.node("members").set(group.getMembers());
            saveConfig();
        } catch (IOException e) {
            Nebula.util.log("Failed to save group configuration: {}.", e.getMessage());
        }
    }

    public void clearMembers(Group group) {
        group.clearMembers();
        saveGroup(group);
    }

    public void addMemberToGroup(Group group, String uuid) {
        group.addMember(uuid);
        saveGroup(group);
    }

    public void removeMemberFromGroup(Group group, String uuid) {
        group.removeMember(uuid);
        saveGroup(group);
    }

    public void addPermissionToGroup(Group group, String permission) {
        group.addMember(permission);
        saveGroup(group);
    }

    public void removePermissionToGroup(Group group, String permission) {
        group.removePermission(permission);
        saveGroup(group);
    }

    public List<String> getGroupMembers(String groupName) {
        return runtimeGroups.stream()
                .filter(group -> group.getName().equals(groupName))
                .findFirst()
                .map(Group::getMembers)
                .orElse(Collections.emptyList());
    }

    public List<String> getGroupNames() {
        return rootNode.node("groups").childrenMap().keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    public ConfigurationNode getGroupNode(String groupName) {
        return rootNode.node("groups", groupName);
    }

    public Group getGroup(String groupName) {
        return runtimeGroups.stream().filter(group -> group.getName().equals(groupName)).findFirst().orElse(null);
    }

    public void saveConfig() {
        try {
            loader.save(rootNode);
        } catch (IOException e) {
            Nebula.util.log("Failed to save configuration: {}.", e.getMessage());
        }
    }
}
