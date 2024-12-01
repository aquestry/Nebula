package de.voasis.nebula;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.voasis.nebula.commands.AdminCommand;
import de.voasis.nebula.commands.QueueCommand;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.data.Util;
import de.voasis.nebula.event.EventManager;
import de.voasis.nebula.helper.*;
import de.voasis.nebula.helper.PermissionManager;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(id = "nebula", name = "Nebula", description = "Nebula can create servers on demand using Docker on multiple machines.", version = "1.0", authors = "Aquestry")
public class Nebula {

    public static ProxyServer server;
    public static ChannelIdentifier channel = MinecraftChannelIdentifier.create("nebula", "main");
    public static FilesManager filesManager;
    public static ServerManager serverManager;
    public static PermissionManager permissionManager;
    public static DefaultsManager defaultsManager;
    public static QueueProcessor queueProcessor;
    public static AutoDeleter autoDeleter;
    public static Util util;

    @Inject
    public Nebula(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        server = proxy;
        permissionManager  = new PermissionManager();
        util = new Util();
        filesManager = new FilesManager(dataDirectory);
        serverManager = new ServerManager();
        defaultsManager = new DefaultsManager();
        defaultsManager.createDefault();
        queueProcessor = new QueueProcessor();
        queueProcessor.init();
        autoDeleter = new AutoDeleter();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        registerCommands();
        util.log(Data.Icon);
        server.getChannelRegistrar().register(channel);
        server.getEventManager().register(this, new EventManager());
        server.getScheduler()
                .buildTask(this, this::Update)
                .repeat(1, TimeUnit.SECONDS)
                .schedule();
    }

    private void Update() {
        util.pingServers();
        queueProcessor.process();
        autoDeleter.process();
    }

    private void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        commandManager.register("admin", new AdminCommand());
        commandManager.register("queue", new QueueCommand());
    }
}