package de.voasis.nebula.Event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Event.Events.*;
import de.voasis.nebula.Nebula;

public class EventManager {

    private final ProxyServer server;

    public EventManager(ProxyServer server) {
        this.server = server;
    }

    @Subscribe
    public void PlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        new PlayerChooseInitialServer(event, server);
    }
    @Subscribe
    public void ServerPreConnect(ServerPreConnectEvent event) {
        new ServerPreConnect(event);
    }
    @Subscribe
    public void ProxyShutdown(ProxyShutdownEvent event) {
        new ProxyShutdown(event, server);
    }
    @Subscribe
    public void LoginEvent(LoginEvent event) {
        new Login(event, server);
    }
    @Subscribe
    public void PlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        new PlayerAvailableCommands(event);
    }
    @Subscribe
    public void CommandExecute(CommandExecuteEvent event) {
        new CommandExecute(event);
    }
    @Subscribe
    public void ServerConnected(ServerConnectedEvent event) {
        new ServerConnected(event);
    }
    @Subscribe
    public void PluginMessage(PluginMessageEvent event) {
        new PluginMessage(event, server);
    }
    @Subscribe
    public void PermissionsSetup(PermissionsSetupEvent event) {
        event.setProvider(Nebula.permissionManager);
    }
}
