package de.voasis.nebula.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.*;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import de.voasis.nebula.event.events.*;
import de.voasis.nebula.Nebula;

public class EventManager {
    @Subscribe
    public void PlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        new PlayerChooseInitialServer(event);
    }
    @Subscribe
    public void ServerPreConnect(ServerPreConnectEvent event) {
        new ServerPreConnect(event);
    }
    @Subscribe
    public void ProxyShutdown(ProxyShutdownEvent event) {
        new ProxyShutdown(event);
    }
    @Subscribe
    public void PlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        new PlayerAvailableCommands(event);
    }
    @Subscribe
    public void ServerConnected(ServerPostConnectEvent event) {
        new ServerPostConnect(event);
    }
    @Subscribe
    public void Disconnect(DisconnectEvent event) {
        new Disconnect(event);
    }
    @Subscribe
    public void PluginMessage(PluginMessageEvent event) {
        new PluginMessage(event);
    }
    @Subscribe
    public void PlayerChat(PlayerChatEvent event) {
        new PlayerChat(event);
    }
    @Subscribe
    public void PermissionsSetup(PermissionsSetupEvent event) {
        event.setProvider(Nebula.permissionManager);
    }
}