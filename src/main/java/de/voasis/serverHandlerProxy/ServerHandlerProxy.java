package de.voasis.serverHandlerProxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
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
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

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

    }
    private void LoadEvents() {
        EventManager eventManager = server.getEventManager();
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
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        logger.info("PostLogin event triggered for player: " + event.getPlayer().getUsername());
        logger.info("Backend-Servers:");
        RegisteredServer defaultServer = null;

        String expectedDefaultServer = ServerHandlerProxy.dataHolder.defaultServer;
        logger.info("Expected default server: " + (expectedDefaultServer != null ? expectedDefaultServer : "not set"));

        for (RegisteredServer server : server.getAllServers()) {
            String serverName = server.getServerInfo().getName();
            logger.info("Found server: '" + serverName + "'");

            if (expectedDefaultServer != null && expectedDefaultServer.equals(serverName)) {
                defaultServer = server;
                logger.info("Default server matched: " + serverName);
                break;
            }
        }

        if (defaultServer != null) {
            final RegisteredServer finalDefaultServer = defaultServer;
            logger.info("Attempting to connect player to default server: " + finalDefaultServer.getServerInfo().getName());
            event.getPlayer().createConnectionRequest(finalDefaultServer).connect()
                    .thenAccept(result -> {
                        if (result.isSuccessful()) {
                            logger.info("Player " + event.getPlayer().getUsername() + " successfully connected to " + finalDefaultServer.getServerInfo().getName());
                        } else {
                            logger.error("Failed to connect player " + event.getPlayer().getUsername() + " to " + finalDefaultServer.getServerInfo().getName() +
                                    ". Reason: " + (result.getReasonComponent().isPresent() ? result.getReasonComponent().get().toString() : "Unknown"));
                            disconnectPlayer(event.getPlayer(), "Failed to connect to the default server. Please try again later.");
                        }
                    })
                    .exceptionally(ex -> {
                        logger.error("Exception occurred while connecting player " + event.getPlayer().getUsername() + " to " + finalDefaultServer.getServerInfo().getName(), ex);
                        if (ex.getCause() instanceof IOException) {
                            logger.error("IO Exception details: " + ex.getCause().getMessage());
                        }
                        disconnectPlayer(event.getPlayer(), "An error occurred while connecting. Please try again later.");
                        return null;
                    });
        } else {
            logger.warn("Default server not found or not set!");
            disconnectPlayer(event.getPlayer(), "Es gibt keine verfügbaren Server mit denen du dich verbinden kannst. Versuche es später erneut oder kontaktiere einen Admin.");
        }
    }

    private void disconnectPlayer(Player player, String reason) {
        logger.info("Disconnecting player " + player.getUsername() + " with reason: " + reason);
        Component disconnectMessage = Component.text(reason).color(NamedTextColor.RED);
        player.disconnect(disconnectMessage);
    }
}
