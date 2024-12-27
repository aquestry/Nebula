package de.voasis.nebula.helper;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Group;
import org.spongepowered.configurate.ConfigurationNode;
import java.util.*;

public class PermissionManager implements PermissionProvider {

    private HashMap<Player, Group> cachedGroups = new HashMap<>();

    public boolean hasPermission(Player player, String permission) {
        for(Group group : Nebula.permissionFile.groups) {
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

    public Group getGroup(Player player, boolean refresh) {
        UUID playerUUID = player.getUniqueId();
        if(cachedGroups.containsKey(player) && !refresh) {
            return cachedGroups.get(player);
        }
        Group highestLevelGroup = null;
        List<Group> sortedGroups = new ArrayList<>(Nebula.permissionFile.groups);
        sortedGroups.sort((g1, g2) -> Integer.compare(g2.getLevel(), g1.getLevel()));
        for (Group group : sortedGroups) {
            List<String> members = Nebula.permissionFile.getGroupMembers(group.getName());
            if (members.contains(playerUUID.toString())) {
                if (highestLevelGroup == null) {
                    highestLevelGroup = group;
                } else if (group.getLevel() > highestLevelGroup.getLevel()) {
                    highestLevelGroup = group;
                }
            }
        }
        if (highestLevelGroup != null) {
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
            Nebula.permissionFile.removeMemberFromGroup(getGroup(player, true), player, null);
            Nebula.permissionFile.addMemberToGroup(group, player, null);
            sendInfotoBackend(player);
        }
    }

    public void createGroup(String name, String prefix, int level) {
        if (getGroupByName(name) == null) {
            Group newGroup = new Group(name, prefix, level);
            Nebula.permissionFile.groups.add(newGroup);
            try {
                ConfigurationNode groupNode = Nebula.permissionFile.getGroupNode(name);
                groupNode.node("prefix").set(prefix);
                groupNode.node("level").set(level);
                groupNode.node("permissions").set(new ArrayList<String>());
                Nebula.permissionFile.saveConfig();
            } catch (Exception e) {
                Nebula.util.log("Error saving group '" + name + "' to the configuration file: " + e.getMessage());
            }
        }
    }

    public void deleteGroup(String name) {
        if (!name.equalsIgnoreCase(Data.defaultGroupName)) {
            Nebula.permissionFile.groups.removeIf(group -> group.getName().equalsIgnoreCase(name));
            try {
                ConfigurationNode rootNode = Nebula.permissionFile.getRootNode();
                rootNode.node("groups").removeChild(name);
                Nebula.permissionFile.saveConfig();
            } catch (Exception e) {
                Nebula.util.log("Error deleting group '" + name + "' from the configuration file: " + e.getMessage());
            }
        }
    }

    public Group getGroupByName(String name) {
        return Nebula.permissionFile.groups.stream()
                .filter(group -> group.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void sendInfotoBackend(Player player) {
        Group group = getGroup(player, true);
        RegisteredServer server = player.getCurrentServer().get().getServer();
        String rankName = group.getPrefix();
        String serverName = server.getServerInfo().getName().split("-")[0];
        String hubName = Nebula.util.getBackendServer(server.getServerInfo().getName()).getHoldServer().getServerName();
        hubName = hubName.substring(0, 1).toUpperCase() + hubName.substring(1);
        serverName = serverName.substring(0, 1).toUpperCase() + serverName.substring(1);
        String info = player.getUsername() + ":" + group.getName() + "#" + group.getLevel() + "#" + group.getPrefix();
        String score = player.getUsername() + "&<blue><bold>Nebula</bold></blue>&<reset>#<white>Rank: " + rankName + "#<white>Service: " + serverName + "#<white>Hub: " + hubName + "#<reset>";
        server.sendPluginMessage(Nebula.channelScore, score.getBytes());
        server.sendPluginMessage(Nebula.channelMain, info.getBytes());
    }
}