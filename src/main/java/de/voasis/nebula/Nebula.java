package de.voasis.nebula;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Commands.AdminCommand;
import de.voasis.nebula.Commands.QueueCommand;
import de.voasis.nebula.Commands.ShutdownCommand;
import de.voasis.nebula.Data.DataHolder;
import de.voasis.nebula.Data.Icon;
import de.voasis.nebula.Data.Util;
import de.voasis.nebula.Event.EventManager;
import de.voasis.nebula.Helper.*;
import de.voasis.nebula.Helper.PermissionManager;
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
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Plugin(id = "nebula", name = "Nebula", description = "Nebula can create servers on demand using Docker on multiple machines.", version = "1.0", authors = "Aquestry")
public class Nebula {
    @Inject
    private ProxyServer server;
    private final Logger logger = LoggerFactory.getLogger("nebula");
    public static YamlDocument config;
    public static DataHolder dataHolder;
    public static ServerManager serverManager;
    public static PermissionManager permissionManager;
    public static DefaultsManager defaultsManager;
    public static QueueProcessor queueProcessor;
    public static AutoDeleter autoDeleter;
    public static Util util;

    @Inject
    public Nebula(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        loadConfig(dataDirectory);
        permissionManager  = new PermissionManager();
        dataHolder = new DataHolder(config, server);
        util = new Util(server, this);
        serverManager = new ServerManager(server);
        dataHolder.Load();
        defaultsManager = new DefaultsManager(server);
        queueProcessor = new QueueProcessor();
        autoDeleter = new AutoDeleter(server);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        registerCommands();
        logger.info(Icon.Icon);
        server.getEventManager().register(this, new EventManager(server));
        server.getScheduler()
                .buildTask(this, this::Update)
                .repeat(1, TimeUnit.SECONDS)
                .schedule();
    }

    private void Update() {
        util.updateState();
        queueProcessor.process();
        autoDeleter.process();
    }

    private void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        commandManager.register("admin", new AdminCommand());
        commandManager.register("shutdown", new ShutdownCommand(server));
        commandManager.register("queue", new QueueCommand());
    }

    public void loadConfig(Path dataDirectory) {
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
        } catch (IOException e) {

            try {
                server.shutdown();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
