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
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Util;
import de.voasis.nebula.Event.EventManager;
import de.voasis.nebula.Helper.*;
import de.voasis.nebula.Helper.PermissionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(id = "nebula", name = "Nebula", description = "Nebula can create servers on demand using Docker on multiple machines.", version = "1.0", authors = "Aquestry")
public class Nebula {

    @Inject
    private ProxyServer server;
    private final Logger logger = LoggerFactory.getLogger("nebula");
    public static FilesManager filesManager;
    public static ServerManager serverManager;
    public static PermissionManager permissionManager;
    public static DefaultsManager defaultsManager;
    public static QueueProcessor queueProcessor;
    public static AutoDeleter autoDeleter;
    public static Util util;

    @Inject
    public Nebula(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        permissionManager  = new PermissionManager();
        filesManager = new FilesManager(server);
        util = new Util(server, this);
        serverManager = new ServerManager(server);
        filesManager.loadFiles(dataDirectory);
        filesManager.Load();
        defaultsManager = new DefaultsManager(server);
        queueProcessor = new QueueProcessor(server);
        autoDeleter = new AutoDeleter();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        registerCommands();
        logger.info(Data.Icon);
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
}
