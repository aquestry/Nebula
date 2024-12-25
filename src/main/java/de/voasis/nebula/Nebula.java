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
import de.voasis.nebula.commands.*;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.data.Util;
import de.voasis.nebula.event.EventManager;
import de.voasis.nebula.helper.*;
import de.voasis.nebula.helper.PermissionManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(id = "nebula", name = "Nebula", description = "Nebula can create servers on demand using Docker on multiple machines.", version = "1.0", authors = "Aquestry")
public class Nebula {

    public static ProxyServer server;
    public static ChannelIdentifier channelMain = MinecraftChannelIdentifier.create("nebula", "main");
    public static ChannelIdentifier channelScore = MinecraftChannelIdentifier.create("nebula", "scoreboard");
    public static FilesManager filesManager;
    public static ServerManager serverManager;
    public static PermissionManager permissionManager;
    public static PermissionFile permissionFile;
    public static DefaultsManager defaultsManager;
    public static QueueProcessor queueProcessor;
    public static AutoDeleter autoDeleter;
    public static PartyManager partyManager;
    public static Util util;
    public static MiniMessage mm;

    @Inject
    public Nebula(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        server = proxy;
        mm = MiniMessage.miniMessage();
        permissionFile = new PermissionFile(dataDirectory);
        permissionManager  = new PermissionManager();
        util = new Util();
        serverManager = new ServerManager();
        filesManager = new FilesManager(dataDirectory);
        defaultsManager = new DefaultsManager();
        defaultsManager.createDefault();
        queueProcessor = new QueueProcessor();
        queueProcessor.init();
        autoDeleter = new AutoDeleter();
        partyManager = new PartyManager();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        registerCommands();
        util.log(Data.Icon);
        server.getChannelRegistrar().register(channelMain);
        server.getChannelRegistrar().register(channelScore);
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
        partyManager.refresh();
    }

    private void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        commandManager.register("admin", new AdminCommand());
        commandManager.register("group", new GroupCommand());
        commandManager.register("lobby", new LobbyCommand());
        commandManager.register("party", new PartyCommand());
        commandManager.register("queue", new QueueCommand());
    }
}