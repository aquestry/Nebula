package de.voasis.nebula.helper;

import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class PermissionFile {

    private final Path configFilePath;
    private ConfigurationLoader<?> loader;
    private ConfigurationNode rootNode;

    public PermissionFile(Path dataDirectory) {
        this.configFilePath = dataDirectory.resolve("perms.conf");
        initializeConfig();
    }

    private void initializeConfig() {
        try {
            if (!Files.exists(configFilePath)) {
                Files.createDirectories(configFilePath.getParent());
                try (InputStream resource = getClass().getClassLoader().getResourceAsStream("perms.conf")) {
                    if (resource != null) {
                        Files.copy(resource, configFilePath);
                        System.out.println("Default perms.conf copied from resources.");
                    } else {
                        System.out.println("Could not find perms.conf in resources.");
                    }
                }
            }
            loader = HoconConfigurationLoader.builder()
                    .file(configFilePath.toFile())
                    .build();
            rootNode = loader.load();
        } catch (IOException e) {
            System.out.println("Failed to initialize configuration: " + e.getMessage());
        }
    }

    public ConfigurationNode getRootNode() {
        return rootNode;
    }

    public void saveConfig() {
        try {
            loader.save(rootNode);
            System.out.println("Configuration saved successfully.");
        } catch (IOException e) {
            System.out.println("Failed to save configuration: " + e.getMessage());
        }
    }

    public ConfigurationLoader<?> getLoader() {
        return loader;
    }

    public Path getConfigFilePath() {
        return configFilePath;
    }
}