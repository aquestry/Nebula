package dev.aquestry.nebula.file;

import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.data.Messages;
import org.spongepowered.configurate.ConfigurationNode;

public class MessageLoader {
    public static void loadMessageStrings(ConfigurationNode messages) {
        try {
            String prefix = messages.node("prefix").getString("[Server] ");
            Messages.PREFIX = prefix;
            // container Messages
            Messages.USAGE_CONTAINER = messages.node("container", "usage").getString("<pre>Usage: /container <stop|delete|template> <args...>").replace("<pre>", prefix);
            Messages.KILL_CONTAINER = messages.node("container", "kill-start").getString("<pre>Killing server instance <name>.").replace("<pre>", prefix);
            Messages.DELETE_CONTAINER = messages.node("container", "delete-start").getString("<pre>Deleting server instance <name>.").replace("<pre>", prefix);
            Messages.CREATE_CONTAINER = messages.node("container", "server-create").getString("<pre>Creating server instance from template...").replace("<pre>", prefix);
            Messages.START_CONTAINER = messages.node("container", "server-start").getString("<pre>Starting server instance <name>.").replace("<pre>", prefix);
            Messages.PULL_TEMPLATE = messages.node("container", "server-pull").getString("<pre>Pulling template <template> on server <name>.").replace("<pre>", prefix);
            Messages.DONE_PULL = messages.node("container", "done-pull").getString("<pre>Done pulling template <template> on server <name>.").replace("<pre>", prefix);
            Messages.ALREADY_EXISTS = messages.node("container", "server-exists").getString("<pre>Server <name> already exists.").replace("<pre>", prefix);
            Messages.SERVER_CONNECT = messages.node("util", "server-connect").getString("<green>Connecting to server <name>...").replace("<pre>", prefix);
            Messages.SERVER_RUNNING = messages.node("container", "server-running").getString("<pre>Server <name> is already running.").replace("<pre>", prefix);
            Messages.SERVER_STOPPED = messages.node("container", "server-stopped").getString("<pre>Server <name> is already stopped.").replace("<pre>", prefix);
            Messages.SERVER_NOT_FOUND = messages.node("container", "server-not-found").getString("<pre>Server <name> not found.").replace("<pre>", prefix);
            Messages.ERROR_CREATE = messages.node("container", "error-create").getString("<pre>Error creating server instance <name>.").replace("<pre>", prefix);
            Messages.ERROR_KILL = messages.node("container", "error-kill").getString("<pre>Error killing server instance <name>.").replace("<pre>", prefix);
            Messages.ERROR_DELETE = messages.node("container", "error-delete").getString("<pre>Error deleting server instance <name>.").replace("<pre>", prefix);
            Messages.ERROR_PULL = messages.node("container", "error-pull").getString("<pre>Error pulling template <template> on server <name>.").replace("<pre>", prefix);
            Messages.ERROR_START = messages.node("container", "error-start").getString("<pre>Error starting server <name>.").replace("<pre>", prefix);
            Messages.DONE = messages.node("container", "done").getString("<pre>Done.").replace("<pre>", prefix);
            Messages.ONLINE = messages.node("util", "server-online").getString("<pre>Server <name> is now online.").replace("<pre>", prefix);
            Messages.OFFLINE = messages.node("util", "server-offline").getString("<pre>Server <name> is now offline.").replace("<pre>", prefix);
            // Queue Messages
            Messages.USAGE_QUEUE = messages.node("queue", "usage").getString("<pre>Usage: /queue leave or /queue join <name>").replace("<pre>", prefix);
            Messages.ADDED_TO_QUEUE = messages.node("queue", "added-to-queue").getString("<pre>You got added to queue: <queue>.").replace("<pre>", prefix);
            Messages.REMOVED_FROM_QUEUE = messages.node("queue", "removed-from-queue").getString("<pre>You got removed from queue: <queue>.").replace("<pre>", prefix);
            Messages.ALREADY_IN_QUEUE = messages.node("queue", "already-in-queue").getString("<pre>You are already in a queue.").replace("<pre>", prefix);
            Messages.NOT_IN_QUEUE = messages.node("queue", "not-in-queue").getString("<pre>You are in no queue.").replace("<pre>", prefix);
            Messages.LOBBY_ONLY = messages.node("queue", "lobby-only").getString("<pre>You can only join a queue from the lobby.").replace("<pre>", prefix);
            Messages.QUEUE_NOT_FOUND = messages.node("queue", "queue-not-found").getString("<pre>Queue not found.").replace("<pre>", prefix);
            // Party Messages
            Messages.TARGET_INVITE_NOT_FOUND = messages.node("party", "target-invite-not-found").getString("<pre>The player <target> is not online.").replace("<pre>", prefix);
            Messages.TARGET_INVITE_ALREADY = messages.node("party", "target-invite-already").getString("<pre>The player <target> is already invited.").replace("<pre>", prefix);
            Messages.SENT_INVITE = messages.node("party", "sent-invite").getString("<pre>You have sent an invite to <target>.").replace("<pre>", prefix);
            Messages.NO_INVITE_FROM_LEADER = messages.node("party", "no-invite-from-leader").getString("<pre>You have no invite from <leader>'s party.").replace("<pre>", prefix);
            Messages.ALREADY_IN_PARTY = messages.node("party", "already-in-party").getString("<pre>You are already in a party. Use /party leave to leave your current party.").replace("<pre>", prefix);
            Messages.JOINED_PARTY = messages.node("party", "joined-party").getString("<pre><player> joined the party of <leader>.").replace("<pre>", prefix);
            Messages.LEFT_PARTY = messages.node("party", "left-party").getString("<pre><player> left the party led by <leader>.").replace("<pre>", prefix);
            Messages.NO_PARTY_TO_LEAVE = messages.node("party", "no-party-to-leave").getString("<pre>You are not in a party to leave.").replace("<pre>", prefix);
            Messages.INVITED_MESSAGE = messages.node("party", "invited-message").getString("<pre>You have been invited to join <leader>'s party.").replace("<pre>", prefix);
            Messages.INVITE_EXPIRED = messages.node("party", "invite-expired").getString("<pre>The invite from <leader>'s party has expired.").replace("<pre>", prefix);
            Messages.INVITE_TO_PLAYER_EXPIRED = messages.node("party", "invite-to-player-expired").getString("<pre>Your invite to <player> has expired.").replace("<pre>", prefix);
            Messages.QUEUE_PLAYER_COUNT_MISMATCH = messages.node("party", "queue-count-not-matching").getString("<pre>The player count in your party does not match the required queue size.").replace("<pre>", prefix);
            Messages.PARTY_NOT_ALLOWED = messages.node("party", "party-not-allowed").getString("<pre>You need to be leader to do that.").replace("<pre>", prefix);
            Messages.USAGE_PARTY = messages.node("party", "command-usage").getString("<pre>Usage: /party <accept|invite|leave> [player].").replace("<pre>", prefix);
            // Group Messages
            Messages.GROUP_USAGE = messages.node("group", "command-usage").getString("<pre>Usage: /group <assign|create|delete|list|permission|info>").replace("<pre>", prefix);
            Messages.GROUP_ASSIGN_SUCCESS = messages.node("group", "assign-success").getString("<pre>Assigned player <player> to group <group>.").replace("<pre>", prefix);
            Messages.GROUP_ASSIGN_PLAYER_NOT_FOUND = messages.node("group", "assign-player-not-found").getString("<pre>Player <player> not found.").replace("<pre>", prefix);
            Messages.GROUP_ASSIGN_GROUP_NOT_FOUND = messages.node("group", "assign-group-not-found").getString("<pre>Group <group> not found.").replace("<pre>", prefix);
            Messages.GROUP_ASSIGN_ALREADY = messages.node("group", "assign-group-already").getString("<pre>Player <player> is already in group <group>.").replace("<pre>", prefix);
            Messages.GROUP_CREATE_SUCCESS = messages.node("group", "create-success").getString("<pre>Group <group> created with prefix <prefix> and level <level>.").replace("<pre>", prefix);
            Messages.GROUP_CREATE_ALREADY_EXISTS = messages.node("group", "create-already-exists").getString("<pre>Group <group> already exists.").replace("<pre>", prefix);
            Messages.GROUP_CREATE_INVALID_LEVEL = messages.node("group", "create-invalid-level").getString("<pre>Invalid group level specified.").replace("<pre>", prefix);
            Messages.GROUP_DELETE_SUCCESS = messages.node("group", "delete-success").getString("<pre>Group <group> deleted.").replace("<pre>", prefix);
            Messages.GROUP_DELETE_DEFAULT = messages.node("group", "delete-default").getString("<pre>Cannot delete the default group.").replace("<pre>", prefix);
            Messages.GROUP_DELETE_NOT_FOUND = messages.node("group", "delete-not-found").getString("<pre>Group <group> not found.").replace("<pre>", prefix);
            Messages.GROUP_LIST_HEADER = messages.node("group", "list-header").getString("<pre>Available groups:").replace("<pre>", prefix);
            Messages.GROUP_LIST_ITEM = messages.node("group", "list-item").getString("<pre>- <group>").replace("<pre>", prefix);
            Messages.GROUP_LIST_EMPTY = messages.node("group", "list-empty").getString("<pre>No groups found.").replace("<pre>", prefix);
            Messages.GROUP_INFO_NOT_FOUND = messages.node("group", "info-not-found").getString("<pre>Group <group> not found.").replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_ADD_SUCCESS = messages.node("group", "permission-add-success").getString("<pre>Permission <permission> added to group <group>.").replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_REMOVE_SUCCESS = messages.node("group", "permission-remove-success").getString("<pre>Permission <permission> removed from group <group>.").replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_LIST_HEADER = messages.node("group", "permission-list-header").getString("<pre>Permissions for group <group>:").replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_REMOVE_NOT_FOUND = messages.node("group", "permission-remove-not-found").getString("<pre>Permission <permission> not found in group <group>.").replace("<pre>", prefix);
            Messages.GROUP_PERMISSION_ALREADY_EXISTS = messages.node("group", "permission-already-exists").getString("<pre>Permission <permission> already exists in group <group>.").replace("<pre>", prefix);
        } catch (Exception e) {
            Nebula.util.log("Error loading message strings", e);
            Nebula.server.shutdown();
            System.exit(0);
        }
    }
}