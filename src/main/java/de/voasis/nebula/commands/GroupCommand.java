package de.voasis.nebula.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.data.Messages;
import de.voasis.nebula.model.Group;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GroupCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length == 0) {
            sendUsageMessage(source);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "assign" -> handleAssign(args, source);
            case "create" -> handleCreate(args, source);
            case "delete" -> handleDelete(args, source);
            case "list" -> handleList(source);
            case "permission" -> handlePermission(args, source);
            case "info" -> handleInfo(args, source);
            default -> sendUsageMessage(source);
        }
    }

    private void handleAssign(String[] args, CommandSource source) {
        if (args.length != 3) {
            sendUsageMessage(source);
            return;
        }
        String playerName = args[1];
        String groupName = args[2];
        Optional<Player> player = Nebula.server.getPlayer(playerName);
        if (player.isEmpty()) {
            Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_PLAYER_NOT_FOUND.replace("<player>", playerName));
            return;
        }
        Group group = Nebula.permissionFile.getGroup(groupName);
        if (group == null) {
            Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_GROUP_NOT_FOUND.replace("<group>", groupName));
            return;
        }
        if (Nebula.permissionManager.getGroup(player.get().getUniqueId().toString()).equals(group)) {
            Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_ALREADY.replace("<group>", groupName).replace("<player>", playerName));
            return;
        }
        Nebula.permissionManager.assignGroup(player.get().getUniqueId().toString(), group);
        Nebula.permissionFile.sendAlltoBackend();
        Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_SUCCESS.replace("<player>", playerName).replace("<group>", groupName));
    }

    private void handleCreate(String[] args, CommandSource source) {
        if (args.length < 3) {
            sendUsageMessage(source);
            return;
        }
        String groupName = args[1];
        int level;
        try {
            level = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            Nebula.util.sendMessage(source, Messages.GROUP_CREATE_INVALID_LEVEL);
            return;
        }
        String prefix = String.join(" ", Stream.of(args).skip(3).toArray(String[]::new));
        if (Nebula.permissionFile.getGroup(groupName) != null) {
            Nebula.util.sendMessage(source, Messages.GROUP_CREATE_ALREADY_EXISTS.replace("<group>", groupName));
            return;
        }
        Nebula.permissionFile.createGroup(groupName, prefix, level);
        Nebula.util.sendMessage(source, Messages.GROUP_CREATE_SUCCESS.replace("<group>", groupName).replace("<prefix>", prefix).replace("<level>", String.valueOf(level)));
    }

    private void handleDelete(String[] args, CommandSource source) {
        if (args.length < 2) {
            sendUsageMessage(source);
            return;
        }
        String groupName = args[1];
        if (groupName.equalsIgnoreCase(Config.defaultGroupName)) {
            Nebula.util.sendMessage(source, Messages.GROUP_DELETE_DEFAULT);
            return;
        }
        if (Nebula.permissionFile.getGroup(groupName) == null) {
            Nebula.util.sendMessage(source, Messages.GROUP_DELETE_NOT_FOUND.replace("<group>", groupName));
            return;
        }
        Nebula.permissionFile.deleteGroup(groupName);
        Nebula.util.sendMessage(source, Messages.GROUP_DELETE_SUCCESS.replace("<group>", groupName));
    }

    private void handleList(CommandSource source) {
        List<String> groupNames = Nebula.permissionFile.getGroupNames();
        if (groupNames.isEmpty()) {
            Nebula.util.sendMessage(source, Messages.GROUP_LIST_EMPTY);
            return;
        }

        Nebula.util.sendMessage(source, Messages.GROUP_LIST_HEADER);
        groupNames.forEach(groupName -> Nebula.util.sendMessage(source, Messages.GROUP_LIST_ITEM.replace("<group>", groupName)));
    }

    private void handleInfo(String[] args, CommandSource source) {
        if (args.length < 2) {
            sendUsageMessage(source);
            return;
        }
        String groupName = args[1];
        Group group = Nebula.permissionFile.getGroup(groupName);
        if (group == null) {
            Nebula.util.sendMessage(source, Messages.GROUP_INFO_NOT_FOUND.replace("<group>", groupName));
            return;
        }

        Nebula.util.logGroupInfo(source, group);
    }

    private void handlePermission(String[] args, CommandSource source) {
        if (args.length < 3) {
            sendUsageMessage(source);
            return;
        }
        String action = args[1].toLowerCase();
        String groupName = args[2];
        Group group = Nebula.permissionFile.getGroup(groupName);
        if (group == null) {
            Nebula.util.sendMessage(source, Messages.GROUP_INFO_NOT_FOUND.replace("<group>", groupName));
            return;
        }
        switch (action) {
            case "add" -> Nebula.permissionFile.addPermissionToGroup(group, args[3]);
            case "remove" -> Nebula.permissionFile.removePermissionToGroup(group, args[3]);
            case "list" -> group.getPermissions().forEach(permission -> Nebula.util.sendMessage(source, permission));
        }
    }

    private void sendUsageMessage(CommandSource source) {
        Nebula.util.sendMessage(source, Messages.GROUP_USAGE);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin") || invocation.source() instanceof ConsoleCommandSource;
    }
}