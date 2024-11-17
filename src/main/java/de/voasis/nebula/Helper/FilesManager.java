package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.GamemodeQueue;
import de.voasis.nebula.Maps.HoldServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilesManager {

    private final Logger logger = LoggerFactory.getLogger("nebula");
    private ConfigurationNode config;
    private ConfigurationNode messages;
    private final ProxyServer server;

    public FilesManager(ProxyServer server) {
        this.server = server;
    }

    public void load() {
        try {
            loadMessageStrings();
            Data.defaultServerTemplate = config.node("lobby-template").getString();
            Data.defaultmax = config.node("lobby-max").getInt();
            Data.defaultmin = config.node("lobby-min").getInt();
            Data.vsecret = config.node("vsecret").getString();
            String envVars = config.node("env-vars").getString();
            Data.envVars = envVars != null ? Arrays.stream(envVars.split(",")).map(s -> " -e "+s).collect(Collectors.joining()) : "";
            String adminList = config.node("admins").getString();
            Data.adminUUIDs = adminList != null ? List.of(adminList.split(",")) : List.of();
            logger.info("Admin UUIDS: {}", Data.adminUUIDs);
            Data.holdServerMap.clear();
            Map<Object, ? extends ConfigurationNode> managerServers = config.node("manager-servers").childrenMap();
            if (managerServers != null) {
                for (Object serverName : managerServers.keySet()) {
                    String ip = config.node("manager-servers", serverName, "ip").getString();
                    String username = config.node("manager-servers", serverName, "username").getString();
                    String password = config.node("manager-servers", serverName, "password").getString();
                    if (ip == null || username == null || password == null) {
                        logger.warn("Incomplete configuration for server '{}'. Skipping this server.", serverName);
                        continue;
                    }
                    HoldServer holdServer = new HoldServer(serverName.toString(), ip, password, 0, username);
                    Data.holdServerMap.add(holdServer);
                    Nebula.util.updateFreePort(holdServer);
                    logger.info("Added hold server to pool: {}", serverName);
                }
            }
            if (Data.holdServerMap.isEmpty()) {
                logger.warn("NO HOLD SERVERS FOUND!!! SHUTTING DOWN!!!");
                server.shutdown();
            }
            Map<Object, ? extends ConfigurationNode> gamemodes = config.node("gamemodes").childrenMap();
            if (gamemodes != null) {
                for (Object queueName : gamemodes.keySet()) {
                    String template = config.node("gamemodes", queueName, "templateName").getString();
                    int neededPlayers = config.node("gamemodes", queueName, "neededPlayers").getInt();
                    Data.alltemplates.add(template);
                    Data.gamemodeQueueMap.add(new GamemodeQueue(queueName.toString(), template, neededPlayers));
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
            String prefix = messages.node("prefix").getString("[Server] ");
            Messages.PREFIX = prefix;
            Messages.NO_PERMISSION = messages.node("no-permission").getString("<pre>You don't have permission to execute this command.").replace("<pre>", prefix);
            Messages.USAGE_ADMIN = messages.node("admin", "usage").getString("<pre>Usage: /admin <stop|delete|template> <args...>").replace("<pre>", prefix);
            Messages.KILL_CONTAINER = messages.node("admin", "kill-start").getString("<pre>Killing server instance <name>.").replace("<pre>", prefix);
            Messages.DELETE_CONTAINER = messages.node("admin", "delete-start").getString("<pre>Deleting server instance <name>.").replace("<pre>", prefix);
            Messages.CREATE_CONTAINER = messages.node("admin", "server-create").getString("<pre>Creating server instance from template...").replace("<pre>", prefix);
            Messages.START_CONTAINER = messages.node("admin", "server-start").getString("<pre>Starting server instance <name>.").replace("<pre>", prefix);
            Messages.PULL_TEMPLATE = messages.node("admin", "server-pull").getString("<pre>Pulling template <template> on server <name>.").replace("<pre>", prefix);
            Messages.ALREADY_EXISTS = messages.node("admin", "server-exists").getString("<pre>Server <name> already exists.").replace("<pre>", prefix);
            Messages.SERVER_CONNECT = messages.node("util", "server-connect").getString("<green>Connecting to server <name>...").replace("<pre>", prefix);
            Messages.SERVER_RUNNING = messages.node("admin", "server-running").getString("<pre>Server <name> is already running.").replace("<pre>", prefix);
            Messages.SERVER_STOPPED = messages.node("admin", "server-stopped").getString("<pre>Server <name> is already stopped.").replace("<pre>", prefix);
            Messages.SERVER_NOT_FOUND = messages.node("admin", "server-not-found").getString("<pre>Server <name> not found.").replace("<pre>", prefix);
            Messages.ERROR_CREATE = messages.node("admin", "error-create").getString("<pre>Error creating server instance <name>.").replace("<pre>", prefix);
            Messages.ERROR_KILL = messages.node("admin", "error-kill").getString("<pre>Error killing server instance <name>.").replace("<pre>", prefix);
            Messages.ERROR_DELETE = messages.node("admin", "error-delete").getString("<pre>Error deleting server instance <name>.").replace("<pre>", prefix);
            Messages.ERROR_PULL = messages.node("admin", "error-pull").getString("<pre>Error pulling template <template> on server <name>.").replace("<pre>", prefix);
            Messages.ERROR_START = messages.node("admin", "error-start").getString("<pre>Error starting server <name>.").replace("<pre>", prefix);
            Messages.DONE = messages.node("admin", "done").getString("<pre>Done.").replace("<pre>", prefix);
            Messages.ONLINE = messages.node("util", "server-online").getString("<pre>Server <name> is now online.").replace("<pre>", prefix);
            Messages.OFFLINE = messages.node("util", "server-offline").getString("<pre>Server <name> is now offline.").replace("<pre>", prefix);
            Messages.USAGE_QUEUE = messages.node("queue", "usage").getString("<pre>Usage: /queue leave or /queue join <name>").replace("<pre>", prefix);
            Messages.ADDED_TO_QUEUE = messages.node("queue", "added-to-queue").getString("<pre>You got added to queue: <queue>.").replace("<pre>", prefix);
            Messages.REMOVED_FROM_QUEUE = messages.node("queue", "removed-from-queue").getString("<pre>You got removed from queue: <queue>.").replace("<pre>", prefix);
            Messages.ALREADY_IN_QUEUE = messages.node("queue", "already-in-queue").getString("<pre>You are already in a queue.").replace("<pre>", prefix);
            Messages.NOT_IN_QUEUE = messages.node("queue", "not-in-queue").getString("<pre>You are in no queue.").replace("<pre>", prefix);
            Messages.LOBBY_ONLY = messages.node("queue", "lobby-only").getString("<pre>You can only join a queue from the lobby.").replace("<pre>", prefix);
            Messages.QUEUE_NOT_FOUND = messages.node("queue", "queue-not-found").getString("<pre>Queue not found.").replace("<pre>", prefix);
            Messages.SHUTDOWN = messages.node("shutdown", "message").getString("<pre>Shutdown! Reason: <reason>").replace("<pre>", prefix);
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
            config = YamlConfigurationLoader.builder().file(configFile).build().load();
            File messagesFile = new File(dataDirectory.toFile(), "messages.yml");
            if (!messagesFile.exists()) {
                copyResource("messages.yml", messagesFile);
            }
            messages = YamlConfigurationLoader.builder().file(messagesFile).build().load();
        } catch (IOException e) {
            logger.error("Error loading configuration files.", e);
            server.shutdown();
        }
    }

    private void copyResource(String resourceName, File destination) {
        try (InputStream resourceStream = getClass().getResourceAsStream("/" + resourceName);
             FileOutputStream outputStream = new FileOutputStream(destination)) {
            if (resourceStream == null) {
                logger.warn("Resource '{}' not found in JAR.", resourceName);
                destination.createNewFile();
            } else {
                resourceStream.transferTo(outputStream);
                logger.info("Copied default '{}' to plugin directory.", resourceName);
            }
        } catch (IOException e) {
            logger.error("Error copying resource file '{}'", resourceName, e);
        }
    }
}
