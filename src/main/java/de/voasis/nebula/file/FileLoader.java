package de.voasis.nebula.file;

import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Proxy;
import de.voasis.nebula.model.Queue;
import de.voasis.nebula.model.Node;
import de.voasis.nebula.Nebula;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class FileLoader {

    private ConfigurationNode config;
    private ConfigurationNode messages;
    private ConfigurationNode multiproxy;

    public FileLoader(Path dataDirectory) {
        loadFiles(dataDirectory);
        load();
    }

    public void load() {
        try {
            MessageLoader.loadMessageStrings(messages);
            Config.defaultServerTemplate = config.node("lobby-template").getString();
            Config.defaultmax = config.node("lobby-max").getInt();
            Config.defaultmin = config.node("lobby-min").getInt();
            Config.pullStart = config.node("pull-start").getBoolean();
            String envVars = config.node("env-vars").getString();
            Config.envVars = (envVars != null && !"none".equals(envVars))
                    ? Arrays.stream(envVars.split(","))
                    .map(s -> " -e " + s)
                    .collect(Collectors.joining())
                    : "";
            loadNodes();
            loadGamemodes(envVars);
            Config.alltemplates.add(Config.defaultServerTemplate);
            if(Config.pullStart) {
                Config.nodeMap.parallelStream().forEach(holdServer ->
                        Config.alltemplates.parallelStream().forEach(template ->
                                Nebula.containerManager.pull(holdServer, template, Nebula.server.getConsoleCommandSource())));
            }
            Config.multiProxyMode = multiproxy.node("enabled").getBoolean();
            if(Config.multiProxyMode) {
                loadProxies();
            }
        } catch (Exception e) {
            Nebula.util.log("Error in configuration loading {}.", e);
            Nebula.server.shutdown();
            System.exit(0);
        }
    }

    private void loadNodes() {
        Map<Object, ? extends ConfigurationNode> managerServers = config.node("nodes").childrenMap();
        if (managerServers != null) {
            for (Object serverName : managerServers.keySet()) {
                String ip = config.node("nodes", serverName, "ip").getString();
                String username = config.node("nodes", serverName, "username").getString();
                String password = config.node("nodes", serverName, "password").getString();
                int port = config.node("nodes", serverName, "port").getInt(22);
                String privateKeyFile = config.node("nodes", serverName, "privateKeyFile").getString();
                if (ip == null || username == null || password == null || privateKeyFile == null || port == 0) {
                    Nebula.util.log("Invalid configuration for node '{}'. Skipping this node.", serverName);
                    continue;
                }
                Node node = new Node(serverName.toString(), ip, username, password, privateKeyFile, port, 0);
                Nebula.ssh.init(node);
                Nebula.ssh.updateFreePort(node);
            }
        }
        if (Config.nodeMap.isEmpty()) {
            Nebula.util.log("No availble nodes, shutting down.");
            Nebula.server.shutdown();
            System.exit(0);
        }
    }

    private void loadGamemodes(String envVars) {
        Map<Object, ? extends ConfigurationNode> gamemodes = config.node("gamemodes").childrenMap();
        if (gamemodes != null) {
            for (Object queueName : gamemodes.keySet()) {
                String template = config.node("gamemodes", queueName, "templateName").getString();
                int neededPlayers = config.node("gamemodes", queueName, "neededPlayers").getInt();
                int preload = config.node("gamemodes", queueName, "preload").getInt();
                String localEnvVars = config.node("gamemodes", queueName, "env-vars").getString();
                if (template == null || neededPlayers == 0 || localEnvVars == null) {
                    Nebula.util.log("Invalid configuration for gamemode '{}'. Skipping this gamemode.", queueName);
                    continue;
                }
                localEnvVars = (localEnvVars != null && !"none".equals(envVars))
                        ? Arrays.stream(envVars.split(","))
                        .map(s -> " -e " + s)
                        .collect(Collectors.joining())
                        : "";
                Config.alltemplates.add(template);
                Config.queueMap.add(new Queue(queueName.toString(), template, neededPlayers, preload, localEnvVars));
                Nebula.util.log("Loaded gamemode {}.", queueName);
            }
        }
    }

    private void loadProxies() {
        Config.HMACSecret = multiproxy.node("hmac-secret").getString();
        Config.multiProxyPort = multiproxy.node("port").getInt();
        Config.multiProxyLevel = (multiproxy.node("level").getInt());
        Map<Object, ? extends ConfigurationNode> proxies = multiproxy.node("proxies").childrenMap();
        if (proxies != null) {
            for (Object proxy : proxies.keySet()) {
                String ip = multiproxy.node("proxies", proxy, "ip").getString();
                int port = multiproxy.node("proxies", proxy, "port").getInt();
                if (port == 0 || ip == null || proxy.toString().equals("THIS")) {
                    Nebula.util.log("Invalid configuration for proxy '{}'. Skipping this proxy.", proxy);
                    continue;
                }
                Config.proxyMap.add(new Proxy(proxy.toString(), ip, port, true));
                Nebula.util.log("Loaded proxy {}.", proxy.toString());
            }
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
            File multiproxyFile = new File(dataDirectory.toFile(), "multi-proxy-config.yml");
            if (!multiproxyFile.exists()) {
                copyResource("multi-proxy-config.yml", multiproxyFile);
            }
            multiproxy = YamlConfigurationLoader.builder().file(multiproxyFile).build().load();
        } catch (IOException e) {
            Nebula.util.log("Error loading configuration files.", e);
            Nebula.server.shutdown();
            System.exit(0);
        }
    }

    private void copyResource(String resourceName, File destination) {
        try (InputStream resourceStream = getClass().getResourceAsStream("/" + resourceName);
            FileOutputStream outputStream = new FileOutputStream(destination)) {
            if (resourceStream == null) {
                Nebula.util.log("Resource '{}' not found in JAR.", resourceName);
                destination.createNewFile();
            } else {
                resourceStream.transferTo(outputStream);
                Nebula.util.log("Copied default '{}' to plugin directory.", resourceName);
            }
        } catch (IOException e) {
            Nebula.util.log("Error copying resource file '{}'", resourceName, e);
        }
    }
}