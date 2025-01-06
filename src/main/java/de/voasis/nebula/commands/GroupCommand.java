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
import java.util.concurrent.CompletableFuture;
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
        String uuid = "";
        Optional<Player> player = Nebula.server.getPlayer(playerName);
        if (player.isEmpty()) {
            uuid = Nebula.util.getUUID(playerName);
            if(uuid.equals("ERROR")) {
                Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_PLAYER_NOT_FOUND.replace("<player>", playerName));
                return;
            }
        }
        Group group = Nebula.permissionFile.getGroup(groupName);
        if (group == null) {
            Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_GROUP_NOT_FOUND.replace("<group>", groupName));
            return;
        }
        Group oldGroup = Nebula.permissionManager.getGroup(uuid);
        if (oldGroup.equals(group)) {
            Nebula.util.sendMessage(source, Messages.GROUP_ASSIGN_ALREADY.replace("<group>", groupName).replace("<player>", playerName));
            return;
        }
        Nebula.permissionManager.assignGroup(uuid, group);
        Nebula.multiProxySender.sendGroup(oldGroup);
        Nebula.multiProxySender.sendGroup(group);
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
        Nebula.multiProxySender.sendGroup(Nebula.permissionFile.createGroup(groupName, prefix, level));
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
        Nebula.multiProxySender.sendDelete(groupName);
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
            case "add":
                Nebula.permissionFile.addPermissionToGroup(group, args[3]);
                Nebula.multiProxySender.sendGroup(group);
            case "remove":
                Nebula.permissionFile.removePermissionToGroup(group, args[3]);
                Nebula.multiProxySender.sendGroup(group);
            case "list":
                group.getPermissions().forEach(permission -> Nebula.util.sendMessage(source, permission));
        }

    }

    private void sendUsageMessage(CommandSource source) {
        Nebula.util.sendMessage(source, Messages.GROUP_USAGE);
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) return CompletableFuture.completedFuture(List.of("assign", "create", "delete", "list", "permission", "info"));
        return switch (args[0].toLowerCase()) {
            case "info", "delete" -> args.length == 2
                    ? CompletableFuture.completedFuture(Nebula.permissionFile.getGroupNames().stream().filter(g -> g.startsWith(args[1])).toList())
                    : CompletableFuture.completedFuture(List.of());
            case "assign" -> CompletableFuture.completedFuture(
                    args.length == 2
                            ? Nebula.server.getAllPlayers().stream().map(Player::getUsername).filter(p -> p.startsWith(args[1])).toList()
                            : args.length == 3
                            ? Nebula.permissionFile.getGroupNames().stream().filter(g -> g.startsWith(args[2])).toList()
                            : List.of()
            );
            case "create" -> CompletableFuture.completedFuture(
                    args.length == 2 ? List.of("<groupName>") : args.length == 3 ? List.of("<level>") : List.of("<prefix...>")
            );
            case "permission" -> CompletableFuture.completedFuture(
                    args.length == 2
                            ? Stream.of("add", "remove", "list").filter(s -> s.startsWith(args[1])).toList()
                            : args.length == 3
                            ? Nebula.permissionFile.getGroupNames().stream().filter(g -> g.startsWith(args[2])).toList()
                            : args.length == 4 && "remove".equalsIgnoreCase(args[1])
                            ? Optional.ofNullable(Nebula.permissionFile.getGroup(args[2]))
                            .map(g -> g.getPermissions().stream().filter(p -> p.startsWith(args[3])).toList())
                            .orElse(List.of())
                            : List.of()
            );
            default -> CompletableFuture.completedFuture(
                    List.of("assign", "create", "delete", "list", "permission", "info").stream()
                            .filter(command -> command.startsWith(args[0].toLowerCase()))
                            .toList()
            );
        };
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin") || invocation.source() instanceof ConsoleCommandSource;
    }
}