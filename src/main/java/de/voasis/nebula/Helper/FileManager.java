package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.HoldServer;
import de.voasis.nebula.Nebula;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FileManager {
    private static final Logger logger = LoggerFactory.getLogger("nebula");
    private final ProxyServer server;
    private final Path dataDirectory;
    private YamlDocument config;
    private YamlDocument messages;

    public FileManager(Path dataDirectory, ProxyServer server) {
        this.dataDirectory = dataDirectory;
        this.server = server;
    }

    public void load() {
        loadConfig();
        loadMessages();
        loadData();
        loadHoldServers();
        logger.info("FileManager: All configurations and data loaded successfully.");
    }

    private void loadConfig() {
        try {
            config = YamlDocument.create(
                    new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .build()
            );
            config.update();
            config.save();
            logger.info("FileManager: config.yml loaded successfully.");
        } catch (IOException e) {
            logger.error("FileManager: Failed to load config.yml", e);
        }
    }

    private void loadMessages() {
        try {
            messages = YamlDocument.create(
                    new File(dataDirectory.toFile(), "messages.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/messages.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("file-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .build()
            );
            messages.update();
            messages.save();
            loadMessageStrings();
            logger.info("FileManager: messages.yml loaded successfully.");
        } catch (IOException e) {
            logger.error("FileManager: Failed to load messages.yml", e);
        }
    }

    private void loadMessageStrings() {
        Messages.USAGE_ADMIN = messages.getString("usage.admin", "Usage: /admin <stop|delete|template> <args...>");
        Messages.USAGE_ADMIN_KILL = messages.getString("usage.admin-kill", "Usage: /admin stop <InstanceName>");
        Messages.USAGE_ADMIN_DELETE = messages.getString("usage.admin-delete", "Usage: /admin delete <InstanceName>");
        Messages.USAGE_ADMIN_TEMPLATE = messages.getString("usage.admin-template", "Usage: /admin template <templateName> <newName>");
        Messages.USAGE_QUEUE = messages.getString("usage.queue", "Usage: /queue leave or /queue join <name>");
        Messages.USAGE_SHUTDOWN = messages.getString("usage.shutdown", "Usage: /shutdown <reason> (optional)");
        Messages.USAGE_UNKNOWN_COMMAND = messages.getString("usage.unknown-command", "Unknown command. Usage: /admin <stop|delete|template> <args...>");

        Messages.FEEDBACK_KILL_START = messages.getString("feedback.kill-start", "Killing server instance...");
        Messages.FEEDBACK_DELETE_START = messages.getString("feedback.delete-start", "Deleting server instance...");
        Messages.FEEDBACK_TEMPLATE_CREATE = messages.getString("feedback.template-create", "Creating server instance from template: <template>");
        Messages.FEEDBACK_TEMPLATE_EXISTS = messages.getString("feedback.template-exists", "Server with the specified name already exists.");
        Messages.FEEDBACK_SERVER_EXISTS = messages.getString("feedback.server-exists", "Server already exists.");
        Messages.FEEDBACK_ADDED_TO_QUEUE = messages.getString("feedback.added-to-queue", "You got added to queue: <queue>.");
        Messages.FEEDBACK_REMOVED_FROM_QUEUE = messages.getString("feedback.removed-from-queue", "You got removed from queue: <queue>.");
        Messages.FEEDBACK_ALREADY_IN_QUEUE = messages.getString("feedback.already-in-queue", "You are already in a queue.");
        Messages.FEEDBACK_NOT_IN_QUEUE = messages.getString("feedback.not-in-queue", "You are in no queue.");
        Messages.FEEDBACK_LOBBY_ONLY = messages.getString("feedback.lobby-only", "You can only join a queue from the lobby.");
        Messages.FEEDBACK_QUEUE_NOT_FOUND = messages.getString("feedback.queue-not-found", "Queue not found.");
        Messages.FEEDBACK_SHUTDOWN = messages.getString("feedback.shutdown", "Shutdown! Reason: <reason>");
        Messages.FEEDBACK_SERVER_OFFLINE = messages.getString("feedback.server-offline", "The server you are trying to connect to is offline.");
        Messages.FEEDBACK_ALREADY_CONNECTED = messages.getString("feedback.already-connected", "You are already connected to that server.");
        Messages.FEEDBACK_SERVER_FULL = messages.getString("feedback.server-full", "The server you are trying to connect to is full.");
        Messages.FEEDBACK_NO_PERMISSION = messages.getString("feedback.no-permission", "You don't have permission to execute this command.");

        Messages.ERROR_SERVER_NOT_FOUND = messages.getString("error.server-not-found", "Server not found.");
        Messages.ERROR_CONTAINER_FAILED = messages.getString("error.container-failed", "Failed to manage Docker container: <container>.");
        Messages.ERROR_NO_PERMISSION_ADMIN = messages.getString("error.no-permission-admin", "You do not have permission to perform this admin action.");
        Messages.ERROR_NO_PERMISSION_QUEUE = messages.getString("error.no-permission-queue", "You do not have permission to join or leave queues.");
        Messages.ERROR_SERVER_KILLED = messages.getString("error.server-killed", "The server you were on was killed.");
    }

    private void loadData() {
        Data.defaultServerTemplate = config.getString("lobby-template");
        Data.defaultmax = config.getInt("lobby-max");
        Data.defaultmin = config.getInt("lobby-min");
        Data.vsecret = config.getString("vsecret");
        Data.adminUUIDs = List.of(config.getString("admins").split(","));
        logger.info("FileManager: Loaded general Data configuration.");
    }

    private void loadHoldServers() {
        Set<Object> managerServerKeys = config.getSection("manager-servers").getKeys();
        for (Object serverName : managerServerKeys) {
            String name = (String) serverName;
            String ip = config.getString("manager-servers." + name + ".ip");
            String password = config.getString("manager-servers." + name + ".password");
            String username = config.getString("manager-servers." + name + ".username");
            HoldServer holdServer = new HoldServer(name, ip, password, 0, username);
            Data.holdServerMap.add(holdServer);
            logger.info("FileManager: Added hold server to pool: {}", name);
        }

        Set<Object> gamemodes = config.getSection("gamemodes").getKeys();
        for (Object queue : gamemodes) {
            String name = (String) queue;
            String template = config.getString("gamemodes." + name + ".templateName");
            int needed = config.getInt("gamemodes." + name + ".neededPlayers");
            Data.alltemplates.add(template);
            Data.gamemodeQueueMap.add(new de.voasis.nebula.Maps.GamemodeQueue(name, template, needed));
            logger.info("FileManager: Added gamemode to pool: {}, {}, {}.", name, template, needed);
        }

        Data.alltemplates.add(Data.defaultServerTemplate);
        for (HoldServer holdServer : Data.holdServerMap) {
            for (String template : Data.alltemplates) {
                Nebula.serverManager.pull(holdServer, template);
            }
        }
    }
}
