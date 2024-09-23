package de.voasis.serverHandlerProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.serverHandlerProxy.Commands.CreateCommand;
import de.voasis.serverHandlerProxy.Commands.DeleteCommand;
import de.voasis.serverHandlerProxy.Commands.StartCommand;
import de.voasis.serverHandlerProxy.Commands.TemplateCommand;
import de.voasis.serverHandlerProxy.Events.PostLogin;
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

@Plugin(id = "serverhandlerproxy", name = "ServerHandlerProxy", version = "1.0", authors = "Aquestry")
public class ServerHandlerProxy {
    @Inject public static Logger logger;
    @Inject public static ProxyServer server;
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

    }
    private void LoadEvents() {
        EventManager eventManager = server.getEventManager();
        eventManager.register(this, new PostLogin());
    }

    @Inject
    public ServerHandlerProxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;

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
}
