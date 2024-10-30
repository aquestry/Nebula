package de.voasis.nebula.Data;

import dev.dejvokep.boostedyaml.YamlDocument;

public class Messages {

    // Usage Messages
    public static String USAGE_ADMIN;
    public static String USAGE_ADMIN_KILL;
    public static String USAGE_ADMIN_DELETE;
    public static String USAGE_ADMIN_TEMPLATE;
    public static String USAGE_QUEUE;
    public static String USAGE_SHUTDOWN;
    public static String USAGE_UNKNOWN_COMMAND;

    // Feedback Messages
    public static String FEEDBACK_KILL_START;
    public static String FEEDBACK_DELETE_START;
    public static String FEEDBACK_TEMPLATE_CREATE;
    public static String FEEDBACK_TEMPLATE_EXISTS;
    public static String FEEDBACK_SERVER_EXISTS;
    public static String FEEDBACK_ADDED_TO_QUEUE;
    public static String FEEDBACK_REMOVED_FROM_QUEUE;
    public static String FEEDBACK_ALREADY_IN_QUEUE;
    public static String FEEDBACK_NOT_IN_QUEUE;
    public static String FEEDBACK_LOBBY_ONLY;
    public static String FEEDBACK_QUEUE_NOT_FOUND;
    public static String FEEDBACK_SHUTDOWN;
    public static String FEEDBACK_SERVER_OFFLINE;
    public static String FEEDBACK_ALREADY_CONNECTED;
    public static String FEEDBACK_SERVER_FULL;
    public static String FEEDBACK_NO_PERMISSION;

    // Error Messages
    public static String ERROR_SERVER_NOT_FOUND;
    public static String ERROR_CONTAINER_FAILED;
    public static String ERROR_NO_PERMISSION_ADMIN;
    public static String ERROR_NO_PERMISSION_QUEUE;
    public static String ERROR_SERVER_KILLED;

    public static void load(YamlDocument messages) {
        // Usage Messages
        USAGE_ADMIN = messages.getString("usage.admin");
        USAGE_ADMIN_KILL = messages.getString("usage.admin-kill");
        USAGE_ADMIN_DELETE = messages.getString("usage.admin-delete");
        USAGE_ADMIN_TEMPLATE = messages.getString("usage.admin-template");
        USAGE_QUEUE = messages.getString("usage.queue");
        USAGE_SHUTDOWN = messages.getString("usage.shutdown");
        USAGE_UNKNOWN_COMMAND = messages.getString("usage.unknown-command");

        // Feedback Messages
        FEEDBACK_KILL_START = messages.getString("feedback.kill-start");
        FEEDBACK_DELETE_START = messages.getString("feedback.delete-start");
        FEEDBACK_TEMPLATE_CREATE = messages.getString("feedback.template-create");
        FEEDBACK_TEMPLATE_EXISTS = messages.getString("feedback.template-exists");
        FEEDBACK_SERVER_EXISTS = messages.getString("feedback.server-exists");
        FEEDBACK_ADDED_TO_QUEUE = messages.getString("feedback.added-to-queue");
        FEEDBACK_REMOVED_FROM_QUEUE = messages.getString("feedback.removed-from-queue");
        FEEDBACK_ALREADY_IN_QUEUE = messages.getString("feedback.already-in-queue");
        FEEDBACK_NOT_IN_QUEUE = messages.getString("feedback.not-in-queue");
        FEEDBACK_LOBBY_ONLY = messages.getString("feedback.lobby-only");
        FEEDBACK_QUEUE_NOT_FOUND = messages.getString("feedback.queue-not-found");
        FEEDBACK_SHUTDOWN = messages.getString("feedback.shutdown");
        FEEDBACK_SERVER_OFFLINE = messages.getString("feedback.server-offline");
        FEEDBACK_ALREADY_CONNECTED = messages.getString("feedback.already-connected");
        FEEDBACK_SERVER_FULL = messages.getString("feedback.server-full");
        FEEDBACK_NO_PERMISSION = messages.getString("feedback.no-permission");

        // Error Messages
        ERROR_SERVER_NOT_FOUND = messages.getString("error.server-not-found");
        ERROR_CONTAINER_FAILED = messages.getString("error.container-failed");
        ERROR_NO_PERMISSION_ADMIN = messages.getString("error.no-permission-admin");
        ERROR_NO_PERMISSION_QUEUE = messages.getString("error.no-permission-queue");
        ERROR_SERVER_KILLED = messages.getString("error.server-killed");
    }
}
