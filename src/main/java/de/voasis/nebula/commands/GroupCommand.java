package de.voasis.nebula.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Group;
import java.util.List;
import java.util.Optional;

public class GroupCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if (!(source instanceof ConsoleCommandSource)) {
            return;
        }
        String[] args = invocation.arguments();
        if (args.length == 0) {
            System.out.println("Usage: /group <assign|create|delete|list|info|permission>");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "assign":
                handleAssign(args);
                break;
            case "create":
                handleCreate(args);
                break;
            case "delete":
                handleDelete(args);
                break;
            case "list":
                handleList();
                break;
            case "info":
                handleInfo(args);
                break;
            case "permission":
                handlePermission(args);
                break;
            default:
                System.out.println("Unknown subcommand. Usage: /group <assign|create|delete|list|info|permission>");
                break;
        }
    }

    private void handleAssign(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: /group assign <player> <group>");
            return;
        }
        String playerName = args[1];
        String groupName = args[2];
        Optional<Player> player = Nebula.server.getPlayer(playerName);
        if (player.isEmpty()) {
            System.out.println("Player not found: " + playerName);
            return;
        }
        Group group = Nebula.permissionManager.getGroupByName(groupName);
        if (group == null) {
            System.out.println("Group not found: " + groupName);
            return;
        }
        Nebula.permissionManager.assignGroup(player.get(), groupName);
        System.out.println("Assigned player " + playerName + " to group " + groupName + ".");
    }

    private void handleCreate(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: /group create <name> <prefix> <level>");
            return;
        }
        String groupName = args[1];
        String prefix = args[2];
        int level;
        try {
            level = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.out.println("Level must be a valid number.");
            return;
        }
        if (Nebula.permissionManager.getGroupByName(groupName) != null) {
            System.out.println("Group already exists: " + groupName);
            return;
        }
        Nebula.permissionManager.createGroup(groupName, prefix, level);
        System.out.println("Group created: " + groupName + " with prefix: " + prefix + " and level: " + level + ".");
    }

    private void handleDelete(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: /group delete <name>");
            return;
        }
        String groupName = args[1];
        if (groupName.equalsIgnoreCase(Data.defaultGroupName)) {
            System.out.println("Cannot delete the default group: " + groupName);
            return;
        }
        if (Nebula.permissionManager.getGroupByName(groupName) == null) {
            System.out.println("Group not found: " + groupName);
            return;
        }
        Nebula.permissionManager.deleteGroup(groupName);
        System.out.println("Group deleted: " + groupName + ".");
    }

    private void handleList() {
        List<String> groupNames = Nebula.permissionFile.getGroupNames();
        if (groupNames.isEmpty()) {
            System.out.println("No groups found.");
            return;
        }
        System.out.println("Available Groups:");
        for (String groupName : groupNames) {
            System.out.println("- " + groupName);
        }
    }

    private void handleInfo(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: /group info <name>");
            return;
        }
        String groupName = args[1];
        Group group = Nebula.permissionManager.getGroupByName(groupName);
        if (group == null) {
            System.out.println("Group not found: " + groupName);
            return;
        }
        Nebula.permissionManager.logGroupInfo(group);
    }

    private void handlePermission(String[] args) {
        if (args.length < 4) {
            System.out.println("Usage: /group permission <add|remove> <group> <permission>");
            return;
        }
        String action = args[1].toLowerCase();
        String groupName = args[2];
        String permission = args[3];
        Group group = Nebula.permissionManager.getGroupByName(groupName);
        if (group == null) {
            System.out.println("Group not found: " + groupName);
            return;
        }
        switch (action) {
            case "add":
                if (!group.hasPermission(permission)) {
                    group.addPermission(permission);
                    Nebula.permissionFile.saveConfig();
                    System.out.println("Permission \"" + permission + "\" added to group: " + groupName);
                } else {
                    System.out.println("Group already has permission: " + permission);
                }
                break;
            case "remove":
                if (group.hasPermission(permission)) {
                    group.getPermissions().remove(permission);
                    Nebula.permissionFile.saveConfig();
                    System.out.println("Permission \"" + permission + "\" removed from group: " + groupName);
                } else {
                    System.out.println("Group does not have permission: " + permission);
                }
                break;
            default:
                System.out.println("Unknown action. Use /group permission <add|remove> <group> <permission>");
                break;
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source() instanceof ConsoleCommandSource;
    }
}