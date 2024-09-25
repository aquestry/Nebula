package de.voasis.serverHandlerProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.serverHandlerProxy.Commands.DeleteCommand;
import de.voasis.serverHandlerProxy.Commands.StartCommand;
import de.voasis.serverHandlerProxy.Commands.StopCommand;
import de.voasis.serverHandlerProxy.Commands.TemplateCommand;
import de.voasis.serverHandlerProxy.Events.EventManager;
import de.voasis.serverHandlerProxy.Helper.DataHolder;
import de.voasis.serverHandlerProxy.Helper.PingUtil;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import de.voasis.serverHandlerProxy.Permission.PermissionManager;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
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
    public PingUtil pingUtil;

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
        createDefaultServer();
        pingUtil = new PingUtil(dataHolder, server, logger, this);
        server.getEventManager().register(this, new EventManager(server, dataHolder, logger, externalServerCreator, permissionManager));
        server.getScheduler()
                .buildTask(this, pingUtil::updateState)
                .repeat(3L, TimeUnit.SECONDS)
                .schedule();
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
        logger.info("External Servers:");
        for (ServerInfo s : dataHolder.serverInfoMap) {
            logger.info(s.getServerName());
        }
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
        logger.info("Creating Default-Server");
        externalServerCreator.createFromTemplate(
                dataHolder.serverInfoMap.getFirst(),
                dataHolder.defaultServer,
                dataHolder.defaultServer,
                "java -jar server.jar -p 25568",
                "stop"
        );
    }
    public void loadConfig(Path dataDirectory) {
        try {
            config = YamlDocument.create(
                    new File(dataDirectory.toFile(), "config.yml"),
                    Objects.requireNonNull(ServerHandlerProxy.class.getResourceAsStream("/config.yml")),
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
            try {
                shutdownPlugin();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
