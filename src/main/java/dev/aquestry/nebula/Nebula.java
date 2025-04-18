package dev.aquestry.nebula;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import dev.aquestry.nebula.commands.*;
import dev.aquestry.nebula.container.AutoDeleter;
import dev.aquestry.nebula.container.ContainerManager;
import dev.aquestry.nebula.container.DefaultsManager;
import dev.aquestry.nebula.feature.PartyManager;
import dev.aquestry.nebula.feature.QueueProcessor;
import dev.aquestry.nebula.file.FileLoader;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.file.PermissionFile;
import dev.aquestry.nebula.feature.Util;
import dev.aquestry.nebula.event.EventManager;
import dev.aquestry.nebula.file.PermissionManager;
import dev.aquestry.nebula.container.ContainerStateChecker;
import dev.aquestry.nebula.network.MultiProxySender;
import dev.aquestry.nebula.network.MultiProxyServer;
import dev.aquestry.nebula.network.SshUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(id = "nebula", name = "Nebula", description = "Nebula can create servers on demand using Docker on multiple machines.", version = "1.0", authors = "Aquestry")
public class Nebula {

    public static ProxyServer server;
    public static ChannelIdentifier channelMain = MinecraftChannelIdentifier.create("nebula", "main");
    public static ChannelIdentifier channelScore = MinecraftChannelIdentifier.create("nebula", "scoreboard");
    public static MiniMessage mm = MiniMessage.miniMessage();
    public static FileLoader fileLoader;
    public static ContainerManager containerManager;
    public static PermissionManager permissionManager;
    public static PermissionFile permissionFile;
    public static DefaultsManager defaultsManager;
    public static QueueProcessor queueProcessor;
    public static AutoDeleter autoDeleter;
    public static PartyManager partyManager;
    public static Util util;
    public static SshUtil ssh;
    public static MultiProxyServer multiProxyServer;
    public static MultiProxySender multiProxySender;
    public static ContainerStateChecker containerStateChecker;

    @Inject
    public Nebula(ProxyServer proxy, @DataDirectory Path dataDirectory) {
        server = proxy;
        ssh = new SshUtil();
        util = new Util();
        containerStateChecker = new ContainerStateChecker();
        permissionFile = new PermissionFile(dataDirectory);
        permissionManager  = new PermissionManager();
        containerManager = new ContainerManager();
        fileLoader = new FileLoader(dataDirectory);
        defaultsManager = new DefaultsManager();
        defaultsManager.createDefault();
        queueProcessor = new QueueProcessor();
        autoDeleter = new AutoDeleter();
        partyManager = new PartyManager();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        registerCommands();
        util.log(Config.Icon);
        server.getChannelRegistrar().register(channelMain);
        server.getChannelRegistrar().register(channelScore);
        server.getEventManager().register(this, new EventManager());
        server.getScheduler()
                .buildTask(this, containerStateChecker::pingServers)
                .repeat(700, TimeUnit.MILLISECONDS)
                .schedule();
        server.getScheduler()
                .buildTask(this, autoDeleter::process)
                .repeat(500, TimeUnit.MILLISECONDS)
                .schedule();
        server.getScheduler()
                .buildTask(this, queueProcessor::process)
                .repeat(1, TimeUnit.SECONDS)
                .schedule();
        server.getScheduler()
                .buildTask(this, partyManager::refresh)
                .repeat(1500, TimeUnit.MILLISECONDS)
                .schedule();
        if(Config.multiProxyMode) {
            multiProxyServer = new MultiProxyServer();
            multiProxySender = new MultiProxySender();
            server.getScheduler()
                    .buildTask(this, multiProxySender::pingProxies)
                    .repeat(1000, TimeUnit.MILLISECONDS)
                    .schedule();
        }
    }

    private void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        commandManager.register("container", new ContainerCommand());
        commandManager.register("lobby", new LobbyCommand(), "leave", "hub");
        commandManager.register("party", new PartyCommand() ,"p");
        commandManager.register("group", new GroupCommand());
        commandManager.register("queue", new QueueCommand());
        if(Config.multiProxyMode) commandManager.register("proxy", new ProxyCommand());
    }
}