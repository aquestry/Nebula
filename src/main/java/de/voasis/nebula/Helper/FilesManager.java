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
                Nebula.serverManager.pull(holdServer, temp);
            }
        }
    }

    private void loadMessageStrings() {
        String prefix = messages.getString("prefix", "[Server] ");
        Messages.PREFIX = prefix;
        Messages.NO_PERMISSION = prefix + messages.getString("no-permission", "You don't have permission to execute this command.");
        Messages.USAGE_ADMIN = prefix + messages.getString("admin.usage", "Usage: /admin <stop|delete|template> <args...>");
        Messages.KILL_CONTAINER = prefix + messages.getString("admin.kill-start", "Killing server instance...");
        Messages.DELETE_CONTAINER = prefix + messages.getString("admin.delete-start", "Deleting server instance...");
        Messages.CREATE_CONTAINER = prefix + messages.getString("admin.server-create", "Creating server instance from template...");
        Messages.PULL_TEMPLATE = prefix + messages.getString("admin.server-pull", "Pulling template.");
        Messages.ALREADY_EXISTS = prefix + messages.getString("admin.server-exists", "Server with the specified name already exists.");
        Messages.SERVER_NOT_FOUND = prefix + messages.getString("admin.server-not-found", "Server not found.");
        Messages.ERROR_CREATE = prefix + messages.getString("admin.error-create", "Error creating server instance.");
        Messages.ERROR_KILL = prefix + messages.getString("admin.error-kill", "Error killing server instance.");
        Messages.ERROR_DELETE = prefix + messages.getString("admin.error-delete", "Error deleting server instance.");
        Messages.ERROR_PULL = prefix + messages.getString("admin.error-pull", "Error pulling server instance.");
        Messages.USAGE_QUEUE = prefix + messages.getString("queue.usage", "Usage: /queue leave or /queue join <name>");
        Messages.ADDED_TO_QUEUE = prefix + messages.getString("queue.added-to-queue", "You got added to queue: <queue>.");
        Messages.REMOVED_FROM_QUEUE = prefix + messages.getString("queue.removed-from-queue", "You got removed from queue: <queue>.");
        Messages.ALREADY_IN_QUEUE = prefix + messages.getString("queue.already-in-queue", "You are already in a queue.");
        Messages.NOT_IN_QUEUE = prefix + messages.getString("queue.not-in-queue", "You are in no queue.");
        Messages.LOBBY_ONLY = prefix + messages.getString("queue.lobby-only", "You can only join a queue from the lobby.");
        Messages.QUEUE_NOT_FOUND = prefix + messages.getString("queue.queue-not-found", "Queue not found.");
        Messages.SHUTDOWN = prefix + messages.getString("shutdown.message", "Shutdown! Reason: <reason>");
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
