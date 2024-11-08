package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.GamemodeQueue;
import de.voasis.nebula.Maps.HoldServer;
import de.voasis.nebula.Nebula;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class FilesManager {

    private final Logger logger = LoggerFactory.getLogger("nebula");
    private YamlConfiguration config;
    private YamlConfiguration messages;
    private final ProxyServer server;

    public FilesManager(ProxyServer server) {
        this.server = server;
    }

    public void Load() {
        try {
            loadMessageStrings();
            Data.defaultServerTemplate = config.getString("lobby-template");
            Data.defaultmax = config.getInt("lobby-max");
            Data.defaultmin = config.getInt("lobby-min");
            Data.vsecret = config.getString("vsecret");
            Data.adminUUIDs = List.of(config.getString("admins").split(","));
            logger.info("Admin UUIDS: {}", Data.adminUUIDs);
            Data.holdServerMap.clear();

            Map<String, Object> managerServers = config.getConfigurationSection("manager-servers").getValues(false);
            if (managerServers != null) {
                for (String serverName : managerServers.keySet()) {
                    String ip = config.getString("manager-servers." + serverName + ".ip");
                    String username = config.getString("manager-servers." + serverName + ".username");
                    String password = config.getString("manager-servers." + serverName + ".password");

                    if (ip == null || username == null || password == null) {
                        logger.warn("Incomplete configuration for server '{}'. Skipping this server.", serverName);
                        continue;
                    }

                    HoldServer holdServer = new HoldServer(serverName, ip, password, 0, username);
                    Data.holdServerMap.add(holdServer);
                    Nebula.util.updateFreePort(holdServer);
                    logger.info("Added hold server to pool: {}", serverName);
                }
            }

            if (Data.holdServerMap.isEmpty()) {
                logger.warn("NO HOLD SERVERS FOUND!!! SHUTTING DOWN!!!");
                server.shutdown();
            }

            Map<String, Object> gamemodes = config.getConfigurationSection("gamemodes").getValues(false);
            if (gamemodes != null) {
                for (String queueName : gamemodes.keySet()) {
                    String template = config.getString("gamemodes." + queueName + ".templateName");
                    int neededPlayers = config.getInt("gamemodes." + queueName + ".neededPlayers");
                    Data.alltemplates.add(template);
                    Data.gamemodeQueueMap.add(new GamemodeQueue(queueName, template, neededPlayers));
                    logger.info("Added gamemode to pool: {}, {}, {}.", queueName, template, neededPlayers);
                }
            }

            Data.alltemplates.add(Data.defaultServerTemplate);

            for (HoldServer holdServer : Data.holdServerMap) {
                for (String template : Data.alltemplates) {
                    Nebula.serverManager.pull(holdServer, template, server.getConsoleCommandSource());
                }
            }
        } catch (Exception e) {
            logger.error("Error in configuration loading", e);
            server.shutdown();
        }
    }

    private void loadMessageStrings() {
        try {
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
            Messages.SERVER_CONNECT = messages.getString("util.server-connect", "<green>Connecting to server <name>...").replace("<pre>", prefix);
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
        } catch (Exception e) {
            logger.error("Error loading message strings", e);
            server.shutdown();
        }
    }

    public void loadFiles(Path dataDirectory) {
        try {
            if (Files.notExists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }

            File configFile = new File(dataDirectory.toFile(), "config.yml");
            if (!configFile.exists()) {
                copyResource("config.yml", configFile);
            }
            config = YamlConfiguration.loadConfiguration(configFile);

            File messagesFile = new File(dataDirectory.toFile(), "messages.yml");
            if (!messagesFile.exists()) {
                copyResource("messages.yml", messagesFile);
            }
            messages = YamlConfiguration.loadConfiguration(messagesFile);

        } catch (IOException e) {
            logger.error("Error loading configuration files.", e);
            server.shutdown();
        }
    }

    private void copyResource(String resourceName, File destination) throws IOException {
        destination.getParentFile().mkdirs();
        try (InputStream resourceStream = getClass().getResourceAsStream("/" + resourceName);
             FileOutputStream outputStream = new FileOutputStream(destination)) {
            if (resourceStream == null) {
                logger.warn("Resource '{}' not found in JAR.", resourceName);
                destination.createNewFile();
            } else {
                resourceStream.transferTo(outputStream);
                logger.info("Copied default '{}' to plugin directory.", resourceName);
            }
        }
    }
}
