package de.voasis.serverHandlerProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.scheduler.ScheduledTask;
import de.voasis.serverHandlerProxy.Commands.CreateCommand;
import de.voasis.serverHandlerProxy.Commands.DeleteCommand;
import de.voasis.serverHandlerProxy.Commands.StartCommand;
import de.voasis.serverHandlerProxy.Commands.TemplateCommand;
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
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        String logo =
                "\n" +
                        "░██████╗██╗░░██╗██████╗░░░░░░░██╗░░░██╗░░███╗░░\n" +
                        "██╔════╝██║░░██║██╔══██╗░░░░░░██║░░░██║░████║░░\n" +
                        "╚█████╗░███████║██████╔╝█████╗╚██╗░██╔╝██╔██║░░\n" +
                        "░╚═══██╗██╔══██║██╔═══╝░╚════╝░╚████╔╝░╚═╝██║░░\n" +
                        "██████╔╝██║░░██║██║░░░░░░░░░░░░░╚██╔╝░░███████╗\n" +
                        "╚═════╝░╚═╝░░╚═╝╚═╝░░░░░░░░░░░░░░╚═╝░░░╚══════╝";
        logger.info(logo);
        logger.info("ServerHandlerProxy started");
        logger.info("External Servers: " + dataHolder.getServerNames());
        assert dataHolder != null;
        CommandManager commandManager = server.getCommandManager();
        commandManager.register("create", new CreateCommand());
        commandManager.register("template", new TemplateCommand());
        commandManager.register("start", new StartCommand());
        commandManager.register("delete", new DeleteCommand());
        logger.info("Commands registered.");
        logger.info("Creating Default-Server...");
        createDefaultServer();
    }
    private void LoadEvents() {
        EventManager eventManager = server.getEventManager();
    }

    private void createDefaultServer() {
        externalServerCreator.createFromTemplate(dataHolder.getAllInfos().getFirst(), dataHolder.defaultServer, dataHolder.defaultServer, "java -jar server.jar -p 25568", "stop");
    }

    @Inject
    public ServerHandlerProxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        dataHolder = new DataHolder();
        externalServerCreator = new ExternalServerCreator(logger, server, dataHolder);

        try {
            config = YamlDocument.create(
                    new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
                    GeneralSettings.DEFAULT, LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT, UpdaterSettings.builder()
                    .setVersioning(new BasicVersioning("file-version"))
                    .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                    .build()
            );
            config.update();
            config.save();
        } catch (IOException e) {
            logger.error("Could not load config! Plugin will shutdown!");
            Optional<PluginContainer> container = server.getPluginManager().getPlugin("serverhandlerproxy");
            container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
        }

        dataHolder.Refresh(config, server, logger);
    }
    private void startDefaultServer() {
        logger.info("Starting Default-Server...");
        externalServerCreator.start(dataHolder.getAllInfos().getFirst(), dataHolder.defaultServer);
    }
    private void deleteDefaultServer() {
        logger.info("Deleting Default-Server...");
        externalServerCreator.delete(dataHolder.getAllInfos().getFirst(), dataHolder.defaultServer);
    }
    @Subscribe
    public void ServerReg(ServerRegisteredEvent event) {
        RegisteredServer reg = event.registeredServer();
        logger.info("Server registered: " + reg.getServerInfo().getName() + ", IP: " + reg.getServerInfo().getAddress());
        if(event.registeredServer().getServerInfo().getName().equals(dataHolder.defaultServer)) {
            logger.info("Default-Server registered.");

            startDefaultServer();
            pingServerUntilOnline(reg, () -> defaultServerstarted(reg.getServerInfo()));
        }
    }
    public Void defaultServerstarted(ServerInfo ser) {
        dataHolder.defaultRegisteredServer = server.registerServer(ser);
        return null;
    }
    public void pingServerUntilOnline(RegisteredServer regServer, Callable<Void> response) {
        final ScheduledTask[] pingTaskHolder = new ScheduledTask[1];

        pingTaskHolder[0] = server.getScheduler().buildTask(this, () -> regServer.ping().whenComplete((result, exception) -> {
            if (exception == null) {
                try {
                    response.call();
                } catch (Exception ignored) {}
                if (pingTaskHolder[0] != null) {
                    pingTaskHolder[0].cancel();
                }
            }
        })).repeat(3, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    public void ServerUnReg(ServerUnregisteredEvent event) {
        RegisteredServer unreg = event.unregisteredServer();
        logger.info("Server unregistered: " + unreg.getServerInfo().getName() + ", IP: " + unreg.getServerInfo().getAddress());
    }
    @Subscribe
    public void ChooseServer(PlayerChooseInitialServerEvent event) {
        Player player = event.getPlayer();
        RegisteredServer defaultServer = dataHolder.defaultRegisteredServer;
        logger.info("Choose Server Event for player: " + player.getUsername());
        if (defaultServer != null) {
            event.setInitialServer(defaultServer);
            logger.info("Default-Server is online connecting player...");
        } else {
            logger.info("Default-Server is offline disconnecting player...");
            player.disconnect(Component.text("Default-Server ist not online"));
        }

    }
    @Subscribe
    public void Shutdown(ProxyShutdownEvent event) {
        deleteDefaultServer();
    }
}
