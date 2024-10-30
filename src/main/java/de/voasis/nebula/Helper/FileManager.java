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
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FileManager {

    private final Logger logger = LoggerFactory.getLogger("nebula");
    private YamlDocument config;
    private YamlDocument messages;
    private final ProxyServer server;
    private MiniMessage mm = MiniMessage.miniMessage();

    public FileManager(YamlDocument config, YamlDocument messages, ProxyServer server) {
        this.config = config;
        this.server = server;
        this.messages = messages;
    }
    public void Load() {
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
            Messages.load(messages);
        } catch (IOException e) {
            try {
                server.shutdown();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

}
