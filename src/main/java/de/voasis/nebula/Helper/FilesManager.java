package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.GamemodeQueue;
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

public class FilesManager {

    private final Logger logger = LoggerFactory.getLogger("nebula");
    private YamlDocument config;
    private YamlDocument messages;
    private final ProxyServer server;

    public FilesManager(ProxyServer server) {
        this.server = server;
    }

    public void Load() {
        loadMessageStrings();
        Data.defaultServerTemplate = config.getString("lobby-template");
        Data.defaultmax = config.getInt("lobby-max");
        Data.defaultmin = config.getInt("lobby-min");
        Data.vsecret = config.getString("vsecret");
        Data.adminUUIDs = List.of(config.getString("admins").split(","));
        logger.info("Admin UUIDS: " + Data.adminUUIDs);
        Set<Object> managerServerKeys = config.getSection("manager-servers").getKeys();
        for (Object serverName : managerServerKeys) {
            String name = (String) serverName;
            String ip = config.getString("manager-servers." + name + ".ip");
            String password = config.getString("manager-servers." + name + ".password");
            String username = config.getString("manager-servers." + name + ".username");
            HoldServer holdServer = new HoldServer(name, ip, password, 0, username);
            Data.holdServerMap.add(holdServer);
            Nebula.util.updateFreePort(holdServer);
            logger.info("Added hold server to pool: {}", name);
        }
        Set<Object> gamemodes = config.getSection("gamemodes").getKeys();
        for (Object queue : gamemodes) {
            String name = (String) queue;
            String template = config.getString("gamemodes." + name + ".templateName");
            int needed = config.getInt("gamemodes." + name + ".neededPlayers");
            Data.alltemplates.add(template);
            Data.gamemodeQueueMap.add(new GamemodeQueue(name, template, needed));
            logger.info("Added gamemode to pool: {}, {}, {}.", name, template, needed);
        }
        Data.alltemplates.add(Data.defaultServerTemplate);
        for(HoldServer holdServer : Data.holdServerMap){
            for(String temp : Data.alltemplates) {
                Nebula.serverManager.pull(holdServer, temp, server.getConsoleCommandSource());
            }
        }
    }

    private void loadMessageStrings() {
        String prefix = messages.getString("prefix", "[Server] ");
        Messages.PREFIX = prefix;
        Messages.NO_PERMISSION = messages.getString("no-permission", "<pre>You don't have permission to execute this command.").replace("<pre>", prefix);
        Messages.USAGE_ADMIN = messages.getString("admin.usage", "<pre>Usage: /admin <stop|delete|template> <args...>").replace("<pre>", prefix);
        Messages.KILL_CONTAINER = messages.getString("admin.kill-start", "<pre>Killing server instance <name>.").replace("<pre>", prefix);
        Messages.DELETE_CONTAINER = messages.getString("admin.delete-start", "<pre>Deleting server instance <name>.").replace("<pre>", prefix);
        Messages.CREATE_CONTAINER = messages.getString("admin.server-create", "<pre>Creating server instance from template...").replace("<pre>", prefix);
        Messages.START_CONTAINER = messages.getString("admin.server-start", "<pre>Starting server instance <name>.").replace("<pre>", prefix);
        Messages.PULL_TEMPLATE = messages.getString("admin.server-pull", "<pre>Pulling template <template> on server <name>.").replace("<pre>", prefix);
        Messages.ALREADY_EXISTS = messages.getString("admin.server-exists", "<pre>Server <name> already exists.").replace("<pre>", prefix);
        Messages.SERVER_RUNNING = messages.getString("admin.server-running", "<pre>Server <name> is already running.").replace("<pre>", prefix);
        Messages.SERVER_STOPPED = messages.getString("admin.server-stopped", "<pre>Server <name> is already stopped.").replace("<pre>", prefix);
        Messages.SERVER_NOT_FOUND = messages.getString("admin.server-not-found", "<pre>Server <name> not found.").replace("<pre>", prefix);
        Messages.ERROR_CREATE = messages.getString("admin.error-create", "<pre>Error creating server instance <name>.").replace("<pre>", prefix);
        Messages.ERROR_KILL = messages.getString("admin.error-kill", "<pre>Error killing server instance <name>.").replace("<pre>", prefix);
        Messages.ERROR_DELETE = messages.getString("admin.error-delete", "<pre>Error deleting server instance <name>.").replace("<pre>", prefix);
        Messages.ERROR_PULL = messages.getString("admin.error-pull", "<pre>Error pulling template <template> on server <name>.").replace("<pre>", prefix);
        Messages.ERROR_START = messages.getString("admin.error-start", "<pre>Error starting server <name>.").replace("<pre>", prefix);
        Messages.DONE = messages.getString("admin.done", "<pre>Done.").replace("<pre>", prefix);
        Messages.ONLINE = messages.getString("util.server-online", "<pre>Server <name> is now online.").replace("<pre>", prefix);
        Messages.OFFLINE = messages.getString("util.server-offline", "<pre>Server <name> is now offline.").replace("<pre>", prefix);
        Messages.USAGE_QUEUE = messages.getString("queue.usage", "<pre>Usage: /queue leave or /queue join <name>").replace("<pre>", prefix);
        Messages.ADDED_TO_QUEUE = messages.getString("queue.added-to-queue", "<pre>You got added to queue: <queue>.").replace("<pre>", prefix);
        Messages.REMOVED_FROM_QUEUE = messages.getString("queue.removed-from-queue", "<pre>You got removed from queue: <queue>.").replace("<pre>", prefix);
        Messages.ALREADY_IN_QUEUE = messages.getString("queue.already-in-queue", "<pre>You are already in a queue.").replace("<pre>", prefix);
        Messages.NOT_IN_QUEUE = messages.getString("queue.not-in-queue", "<pre>You are in no queue.").replace("<pre>", prefix);
        Messages.LOBBY_ONLY = messages.getString("queue.lobby-only", "<pre>You can only join a queue from the lobby.").replace("<pre>", prefix);
        Messages.QUEUE_NOT_FOUND = messages.getString("queue.queue-not-found", "<pre>Queue not found.").replace("<pre>", prefix);
        Messages.SHUTDOWN = messages.getString("shutdown.message", "<pre>Shutdown! Reason: <reason>").replace("<pre>", prefix);
    }


    public void loadFiles(Path dataDirectory) {
        try {
            config = YamlDocument.create(
                    new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(Nebula.class.getResourceAsStream("/config.yml")),
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
            messages = YamlDocument.create(
                    new File(dataDirectory.toFile(), "messages.yml"),
                    Objects.requireNonNull(Nebula.class.getResourceAsStream("/messages.yml")),
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
        } catch (IOException e) {
            try {
                server.shutdown();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
