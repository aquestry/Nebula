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
import de.voasis.nebula.container.AutoDeleter;
import de.voasis.nebula.container.ContainerManager;
import de.voasis.nebula.container.DefaultsManager;
import de.voasis.nebula.container.QueueProcessor;
import de.voasis.nebula.manager.FilesManager;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.manager.PermissionFile;
import de.voasis.nebula.manager.Util;
import de.voasis.nebula.event.EventManager;
import de.voasis.nebula.manager.*;
import de.voasis.nebula.manager.PermissionManager;
import de.voasis.nebula.network.ContainerStateChecker;
import de.voasis.nebula.network.MultiProxySender;
import de.voasis.nebula.network.MultiProxyServer;
import de.voasis.nebula.network.SshUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Plugin(id = "nebula", name = "Nebula", description = "Nebula can create servers on demand using Docker on multiple machines.", version = "1.0", authors = "Aquestry")
public class Nebula {

    public static ProxyServer server;
    public static ChannelIdentifier channelMain = MinecraftChannelIdentifier.create("nebula", "main");
    public static ChannelIdentifier channelScore = MinecraftChannelIdentifier.create("nebula", "scoreboard");
    public static MiniMessage mm = MiniMessage.miniMessage();
    public static FilesManager filesManager;
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
        filesManager = new FilesManager(dataDirectory);
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
                .buildTask(this, queueProcessor::process)
                .repeat(300, TimeUnit.MILLISECONDS)
                .schedule();
        server.getScheduler()
                .buildTask(this, autoDeleter::process)
                .repeat(500, TimeUnit.MILLISECONDS)
                .schedule();
        server.getScheduler()
                .buildTask(this, partyManager::refresh)
                .repeat(1500, TimeUnit.MILLISECONDS)
                .schedule();
        if(Config.multiProxyMode) {
            multiProxyServer = new MultiProxyServer();
            multiProxySender = new MultiProxySender();
            server.getScheduler()
                    .buildTask(this, multiProxySender::pingProxys)
                    .repeat(1000, TimeUnit.MILLISECONDS)
                    .schedule();
        }
    }

    private void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        commandManager.register("node", new NodeCommand());
        commandManager.register("group", new GroupCommand());
        commandManager.register("lobby", new LobbyCommand());
        commandManager.register("party", new PartyCommand());
        commandManager.register("p", new PartyCommand());
        commandManager.register("queue", new QueueCommand());
    }
}