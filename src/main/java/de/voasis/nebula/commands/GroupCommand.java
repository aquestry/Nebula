package de.voasis.nebula.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.data.Messages;
import de.voasis.nebula.map.Group;
import java.util.List;
import java.util.Optional;

public class GroupCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length == 0) {
            Nebula.util.sendMessage(source, Messages.GROUP_USAGE);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "assign":
                handleAssign(args, source);
                break;
            case "create":
                handleCreate(args, source);
                break;
            case "delete":
                handleDelete(args, source);
                break;
            case "list":
                handleList(source);
                break;
            case "permission":
                handlePermission(args, source);
                break;
            case "info":
                handleInfo(args, source);
                break;
            default:
                Nebula.util.sendMessage(source, Messages.GROUP_USAGE);
                break;
        }
    }

    private void handleAssign(String[] args, CommandSource source) {
        if (args.length < 3) {
            Nebula.util.sendMessage(source, Messages.GROUP_USAGE);
            return;
        }
        String playerName = args[1];
        String groupName = args[2];
        Optional<Player> player = Nebula.server.getPlayer(playerName);
        if (player.isEmpty()) {
            Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_PLAYER_NOT_FOUND.replace("<player>", playerName));
            return;
        }
        Group group = Nebula.permissionManager.getGroupByName(groupName);
        if (group == null) {
            Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_GROUP_NOT_FOUND.replace("<group>", groupName));
            return;
        }
        Nebula.permissionManager.assignGroup(player.get(), groupName);
        Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_SUCCESS.replace("<player>", playerName).replace("<group>", groupName));
    }

    private void handleCreate(String[] args, CommandSource source) {
        if (args.length < 4) {
            Nebula.util.sendMessage(source, Messages.GROUP_USAGE);
            return;
        }
        String groupName = args[1];
        String prefix = args[2];
        int level;
        try {
            level = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            Nebula.util.sendMessage(source, Messages.GROUP_CREATE_INVALID_LEVEL);
            return;
        }
        if (Nebula.permissionManager.getGroupByName(groupName) != null) {
            Nebula.util.sendMessage(source, Messages.GROUP_CREATE_ALREADY_EXISTS.replace("<group>", groupName));
            return;
        }
        Nebula.permissionManager.createGroup(groupName, prefix, level);
        Nebula.util.sendMessage(source, Messages.GROUP_CREATE_SUCCESS.replace("<group>", groupName).replace("<prefix>", prefix).replace("<level>", String.valueOf(level)));
    }

    private void handleDelete(String[] args, CommandSource source) {
        if (args.length < 2) {
            Nebula.util.sendMessage(source, Messages.GROUP_USAGE);
            return;
        }
        String groupName = args[1];
        if (groupName.equalsIgnoreCase(Data.defaultGroupName)) {
            Nebula.util.sendMessage(source, Messages.GROUP_DELETE_DEFAULT);
            return;
        }
        if (Nebula.permissionManager.getGroupByName(groupName) == null) {
            Nebula.util.sendMessage(source, Messages.GROUP_DELETE_NOT_FOUND.replace("<group>", groupName));
            return;
        }
        Nebula.permissionManager.deleteGroup(groupName);
        Nebula.util.sendMessage(source, Messages.GROUP_DELETE_SUCCESS.replace("<group>", groupName));
    }

    private void handleList(CommandSource source) {
        List<String> groupNames = Nebula.permissionFile.getGroupNames();
        if (groupNames.isEmpty()) {
            Nebula.util.sendMessage(source, Messages.GROUP_LIST_EMPTY);
            return;
        }
        Nebula.util.sendMessage(source, Messages.GROUP_LIST_HEADER);
        for (String groupName : groupNames) {
            Nebula.util.sendMessage(source, Messages.GROUP_LIST_ITEM.replace("<group>", groupName));
        }
    }

    private void handleInfo(String[] args, CommandSource source) {
        if (args.length < 2) {
            Nebula.util.sendMessage(source, Messages.GROUP_USAGE);
            return;
        }
        String groupName = args[1];
        Group group = Nebula.permissionManager.getGroupByName(groupName);
        if (group == null) {
            Nebula.util.sendMessage(source, Messages.GROUP_INFO_NOT_FOUND.replace("<group>", groupName));
            return;
        }
        Nebula.permissionManager.logGroupInfo(source, group);
    }

    private void handlePermission(String[] args, CommandSource source) {
        if (args.length < 3) {
            Nebula.util.sendMessage(source, Messages.GROUP_USAGE);
            return;
        }
        String action = args[1].toLowerCase();
        String groupName = args[2];
        Group group = Nebula.permissionManager.getGroupByName(groupName);
        if (group == null) {
            Nebula.util.sendMessage(source, Messages.GROUP_INFO_NOT_FOUND.replace("<group>", groupName));
            return;
        }
        switch (action) {
            case "add":
                String permissionToAdd = args[3];
                group.addPermission(permissionToAdd);
                Nebula.util.sendMessage(source, Messages.GROUP_PERMISSION_ADD_SUCCESS.replace("<permission>", permissionToAdd));
                break;
            case "remove":
                String permissionToRemove = args[3];
                group.removePermission(permissionToRemove);
                Nebula.util.sendMessage(source, Messages.GROUP_PERMISSION_REMOVE_SUCCESS.replace("<permission>", permissionToRemove));
                break;
            case "list":
                Nebula.util.sendMessage(source, Messages.GROUP_PERMISSION_LIST_HEADER.replace("<group>", groupName));
                for(String permission : group.getPermissions()) {
                    Nebula.util.sendMessage(source, permission);
                }
                break;
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        CommandSource sender = invocation.source();
        return sender.hasPermission("velocity.admin") || sender instanceof ConsoleCommandSource;
    }
}