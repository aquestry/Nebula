package de.voasis.serverHandlerProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Commands.DeleteCommand;
import de.voasis.serverHandlerProxy.Commands.StartCommand;
import de.voasis.serverHandlerProxy.Commands.StopCommand;
import de.voasis.serverHandlerProxy.Commands.TemplateCommand;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Permission.PermissionManager;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Plugin(id = "serverhandlerproxy", name = "ServerHandlerProxy", version = "1.0", authors = "Aquestry")
public class ServerHandlerProxy {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer server;
    public static YamlDocument config;
    public static DataHolder dataHolder;
    public static ExternalServerCreator externalServerCreator;
    public PermissionManager permissionManager;
    @Inject
    public ServerHandlerProxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        dataHolder = new DataHolder();
        externalServerCreator = new ExternalServerCreator(logger, server, dataHolder);
        loadConfig(dataDirectory);
        dataHolder.Refresh(config, server, logger);
        permissionManager  = new PermissionManager();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logStartup();
        registerCommands();
        logger.info("Creating Default-Server...");
        createDefaultServer();
        server.getScheduler()
                .buildTask(this, this::updateState)
                .repeat(3L, TimeUnit.SECONDS)
                .schedule();
    }

    private void loadConfig(Path dataDirectory) {
        try {
            config = YamlDocument.create(
                    new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
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
            logger.error("Could not load config! Plugin will shutdown!");
            shutdownPlugin();
        }
    }

    private void shutdownPlugin() {
        Optional<PluginContainer> container = server.getPluginManager().getPlugin("serverhandlerproxy");
        container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
    }

    private void logStartup() {
        String logo = """
                \n
                ░██████╗██╗░░██╗██████╗░░░░░░░██╗░░░██╗░░███╗░░
                ██╔════╝██║░░██║██╔══██╗░░░░░░██║░░░██║░████║░░
                ╚█████╗░███████║██████╔╝█████╗╚██╗░██╔╝██╔██║░░
                ░╚═══██╗██╔══██║██╔═══╝░╚════╝░╚████╔╝░╚═╝██║░░
                ██████╔╝██║░░██║██║░░░░░░░░░░░░░╚██╔╝░░███████╗
                ╚═════╝░╚═╝░░╚═╝╚═╝░░░░░░░░░░░░░░╚═╝░░░╚══════╝
                """;
        logger.info(logo);
        logger.info("ServerHandlerProxy started");
        logger.info("External Servers: " + dataHolder.serverInfoMap);
    }

    private void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        commandManager.register("template", new TemplateCommand());
        commandManager.register("start", new StartCommand());
        commandManager.register("delete", new DeleteCommand());
        commandManager.register("stopserver", new StopCommand());
        logger.info("Commands registered.");
    }

    private void createDefaultServer() {
        externalServerCreator.createFromTemplate(
                dataHolder.serverInfoMap.getFirst(),
                dataHolder.defaultServer,
                dataHolder.defaultServer,
                "java -jar server.jar -p 25568",
                "stop"
        );
    }

    @Subscribe
    public void onServerRegistered(ServerRegisteredEvent event) {
        RegisteredServer reg = event.registeredServer();
        logger.info("Server registered: " + reg.getServerInfo().getName() + ", IP: " + reg.getServerInfo().getAddress());
        if (reg.getServerInfo().getName().equals(dataHolder.defaultServer)) {
            logger.info("Default-Server registered.");
            dataHolder.defaultRegisteredServer = server.registerServer(reg.getServerInfo());
            startDefaultServer();
        }
    }
    public void updateState() {
        for(BackendServer backendServer : dataHolder.backendInfoMap) {
            Optional<RegisteredServer> r = server.getServer(backendServer.getServerName());
            r.ifPresent(registeredServer -> pingServer(registeredServer,stateComplete(registeredServer), stateCompleteFailed(registeredServer)));
        }
    }
    public Callable<Void> stateComplete(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (!backendServer.getState()) {
                        backendServer.setState(true);
                        logger.info("Server: " + backendServer.getServerName() + ", is now online.");
                    }
                }
            }
            return null;
        };
    }

    public Callable<Void> stateCompleteFailed(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (backendServer.getState()) {
                        backendServer.setState(false);
                        logger.info("Server: " + backendServer.getServerName() + ", is now offline.");
                    }
                }
            }
            return null;
        };
    }

    @Subscribe
    public void onServerUnregistered(ServerUnregisteredEvent event) {
        RegisteredServer unreg = event.unregisteredServer();
        dataHolder.serverInfoMap.removeIf(serverInfo -> serverInfo.getServerName().equals(unreg.getServerInfo().getName()));
        logger.info("Server unregistered: " + unreg.getServerInfo().getName() + ", IP: " + unreg.getServerInfo().getAddress());
    }

    @Subscribe
    public void onChooseServer(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        RegisteredServer defaultServer = dataHolder.defaultRegisteredServer;
        logger.info("Choose Server Event for player: " + player.getUsername());

        if (defaultServer != null) {
            BackendServer info = dataHolder.getBackendServer(defaultServer.getServerInfo().getName());
            if(info != null) {
                if(info.getState()) {
                    event.setInitialServer(defaultServer);
                    logger.info("Default-Server is online, connecting player...");
                    return;
                }
            }
        }
        logger.info("Default-Server is offline, disconnecting player...");
        player.disconnect(Component.text("Default-Server is not online"));
    }
    @Subscribe
    public void preConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer target = event.getOriginalServer();
        if(!dataHolder.getState(target.getServerInfo().getName())) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        deleteDefaultServer();
    }

    @Subscribe
    public void Perm(PermissionsSetupEvent event) {
        event.setProvider(permissionManager);
    }
    @Subscribe
    public void beforeSwitch(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
    }
    @Subscribe
    public void onPlayerJoin(LoginEvent event) {
        Player player = event.getPlayer();
        if(dataHolder.admins.contains(player.getUniqueId().toString())) {
            permissionManager.addPermission(player, "velocity.admin");
            permissionManager.addPermission(player, "velocity.command.plugins");
            permissionManager.addPermission(player, "velocity.command.info");
            permissionManager.addPermission(player, "velocity.command.server");
            permissionManager.addPermission(player, "velocity.command.send");
            permissionManager.addPermission(player, "velocity.command.glist");
        }

        logger.info("Player is admin: " + player.hasPermission("velocity.admin"));
    }
    private void startDefaultServer() {
        logger.info("Starting Default-Server...");
        externalServerCreator.start(dataHolder.serverInfoMap.getFirst(), dataHolder.defaultServer);
    }

    private void deleteDefaultServer() {
        logger.info("Deleting Default-Server...");
        externalServerCreator.delete(dataHolder.serverInfoMap.getFirst(), dataHolder.defaultServer);
    }

    private void pingServer(RegisteredServer regServer, Callable<Void> response, Callable<Void> noResponse) {
        regServer.ping().whenComplete((result, exception) -> {
            if (exception == null) {
                try {
                    synchronized (this) {
                        response.call();
                    }
                } catch (Exception e) {
                    logger.error("Error while executing success response for server: " + regServer.getServerInfo().getName(), e);
                }
            } else {
                try {
                    synchronized (this) {
                        noResponse.call();
                    }
                } catch (Exception e) {
                    logger.error("Error while executing failure response for server: " + regServer.getServerInfo().getName(), e);
                }
            }
        });
    }

}
