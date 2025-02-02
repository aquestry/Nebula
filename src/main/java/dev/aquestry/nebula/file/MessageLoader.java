package dev.aquestry.nebula.file;

import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.data.Messages;
import org.spongepowered.configurate.ConfigurationNode;

public class MessageLoader {
    public static void loadMessageStrings(ConfigurationNode messages) {
        try {
            String prefix = messages.node("prefix").getString("[Server] ");
            Messages.PREFIX = prefix;
            // Container Messages
            Messages.USAGE_CONTAINER = messages.node("container", "usage").getString().replace("<pre>", prefix);
            Messages.KILL_CONTAINER = messages.node("container", "kill-start").getString().replace("<pre>", prefix);
            Messages.DELETE_CONTAINER = messages.node("container", "delete-start").getString().replace("<pre>", prefix);
            Messages.CREATE_CONTAINER = messages.node("container", "server-create").getString().replace("<pre>", prefix);
            Messages.START_CONTAINER = messages.node("container", "server-start").getString().replace("<pre>", prefix);
            Messages.PULL_TEMPLATE = messages.node("container", "server-pull").getString().replace("<pre>", prefix);
            Messages.DONE_PULL = messages.node("container", "done-pull").getString().replace("<pre>", prefix);
            Messages.ALREADY_EXISTS = messages.node("container", "server-exists").getString().replace("<pre>", prefix);
            Messages.SERVER_CONNECT = messages.node("util", "server-connect").getString().replace("<pre>", prefix);
            Messages.SERVER_RUNNING = messages.node("container", "server-running").getString().replace("<pre>", prefix);
            Messages.SERVER_STOPPED = messages.node("container", "server-stopped").getString().replace("<pre>", prefix);
            Messages.SERVER_NOT_FOUND = messages.node("container", "server-not-found").getString().replace("<pre>", prefix);
            Messages.ERROR_CREATE = messages.node("container", "error-create").getString().replace("<pre>", prefix);
            Messages.ERROR_KILL = messages.node("container", "error-kill").getString().replace("<pre>", prefix);
            Messages.ERROR_DELETE = messages.node("container", "error-delete").getString().replace("<pre>", prefix);
            Messages.ERROR_PULL = messages.node("container", "error-pull").getString().replace("<pre>", prefix);
            Messages.ERROR_START = messages.node("container", "error-start").getString().replace("<pre>", prefix);
            Messages.DONE = messages.node("container", "done").getString().replace("<pre>", prefix);
            Messages.ONLINE = messages.node("util", "server-online").getString().replace("<pre>", prefix);
            Messages.OFFLINE = messages.node("util", "server-offline").getString().replace("<pre>", prefix);
            // Queue Messages
            Messages.USAGE_QUEUE = messages.node("queue", "usage").getString().replace("<pre>", prefix);
            Messages.ADDED_TO_QUEUE = messages.node("queue", "added-to-queue").getString().replace("<pre>", prefix);
            Messages.REMOVED_FROM_QUEUE = messages.node("queue", "removed-from-queue").getString().replace("<pre>", prefix);
            Messages.ALREADY_IN_QUEUE = messages.node("queue", "already-in-queue").getString().replace("<pre>", prefix);
            Messages.NOT_IN_QUEUE = messages.node("queue", "not-in-queue").getString().replace("<pre>", prefix);
            Messages.LOBBY_ONLY = messages.node("queue", "lobby-only").getString().replace("<pre>", prefix);
            Messages.QUEUE_NOT_FOUND = messages.node("queue", "queue-not-found").getString().replace("<pre>", prefix);
            // Party Messages
            Messages.TARGET_INVITE_NOT_FOUND = messages.node("party", "target-invite-not-found").getString().replace("<pre>", prefix);
            Messages.TARGET_INVITE_ALREADY = messages.node("party", "target-invite-already").getString().replace("<pre>", prefix);
            Messages.SENT_INVITE = messages.node("party", "sent-invite").getString().replace("<pre>", prefix);
            Messages.NO_INVITE_FROM_LEADER = messages.node("party", "no-invite-from-leader").getString().replace("<pre>", prefix);
            Messages.ALREADY_IN_PARTY = messages.node("party", "already-in-party").getString().replace("<pre>", prefix);
            Messages.JOINED_PARTY = messages.node("party", "joined-party").getString().replace("<pre>", prefix);
            Messages.LEFT_PARTY = messages.node("party", "left-party").getString().replace("<pre>", prefix);
            Messages.NO_PARTY_TO_LEAVE = messages.node("party", "no-party-to-leave").getString().replace("<pre>", prefix);
            Messages.INVITED_MESSAGE = messages.node("party", "invited-message").getString().replace("<pre>", prefix);
            Messages.INVITE_NOT_ACCEPT = messages.node("party", "invite-not-accept").getString().replace("<pre>", prefix);
            Messages.INVITE_TO_PLAYER_NOT_ACCEPT = messages.node("party", "invite-to-player-not-accept").getString("Error").replace("<pre>", prefix);
            Messages.QUEUE_PLAYER_COUNT_MISMATCH = messages.node("party", "queue-count-not-matching").getString().replace("<pre>", prefix);
            Messages.PARTY_NOT_ALLOWED = messages.node("party", "party-not-allowed").getString().replace("<pre>", prefix);
            Messages.USAGE_PARTY = messages.node("party", "command-usage").getString().replace("<pre>", prefix);
            // Group Messages
            Messages.GROUP_USAGE = messages.node("group", "command-usage").getString().replace("<pre>", prefix);
            Messages.GROUP_ASSIGN_SUCCESS = messages.node("group", "assign-success").getString().replace("<pre>", prefix);
            Messages.GROUP_ASSIGN_PLAYER_NOT_FOUND = messages.node("group", "assign-player-not-found").getString().replace("<pre>", prefix);
            Messages.GROUP_ASSIGN_GROUP_NOT_FOUND = messages.node("group", "assign-group-not-found").getString().replace("<pre>", prefix);
            Messages.GROUP_ASSIGN_ALREADY = messages.node("group", "assign-group-already").getString().replace("<pre>", prefix);
            Messages.GROUP_CREATE_SUCCESS = messages.node("group", "create-success").getString().replace("<pre>", prefix);
            Messages.GROUP_CREATE_ALREADY_EXISTS = messages.node("group", "create-already-exists").getString().replace("<pre>", prefix);
            Messages.GROUP_CREATE_INVALID_LEVEL = messages.node("group", "create-invalid-level").getString().replace("<pre>", prefix);
            Messages.GROUP_DELETE_SUCCESS = messages.node("group", "delete-success").getString().replace("<pre>", prefix);
            Messages.GROUP_DELETE_DEFAULT = messages.node("group", "delete-default").getString().replace("<pre>", prefix);
            Messages.GROUP_DELETE_NOT_FOUND = messages.node("group", "delete-not-found").getString().replace("<pre>", prefix);
            Messages.GROUP_LIST_HEADER = messages.node("group", "list-header").getString().replace("<pre>", prefix);
            Messages.GROUP_LIST_ITEM = messages.node("group", "list-item").getString().replace("<pre>", prefix);
            Messages.GROUP_LIST_EMPTY = messages.node("group", "list-empty").getString().replace("<pre>", prefix);
            Messages.GROUP_INFO_NOT_FOUND = messages.node("group", "info-not-found").getString().replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_ADD_SUCCESS = messages.node("group", "permission-add-success").getString().replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_REMOVE_SUCCESS = messages.node("group", "permission-remove-success").getString().replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_LIST_HEADER = messages.node("group", "permission-list-header").getString().replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_REMOVE_NOT_FOUND = messages.node("group", "permission-remove-not-found").getString().replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_ALREADY_EXISTS = messages.node("group", "permission-already-exists").getString().replace("<pre>", prefix);
        } catch (Exception e) {
            Nebula.util.log("Error loading messages.", e);
            Nebula.server.shutdown();
            System.exit(0);
        }
    }
}