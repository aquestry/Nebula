package de.voasis.nebula.Event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
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
    public void onChooseServer(PlayerChooseInitialServerEvent event) {
        new PlayerChooseInitialServer(event, server);
    }
    @Subscribe
    public void preConnect(ServerPreConnectEvent event) {
        new ServerPreConnect(event);
    }
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        new ProxyShutdown(event, server);
    }
    @Subscribe
    public void Perm(PermissionsSetupEvent event) {
        event.setProvider(Nebula.permissionManager);
    }
    @Subscribe
    public void onPlayerJoin(LoginEvent event) {
        new Login(event, server);
    }
    @Subscribe
    public void onCommandLoad(PlayerAvailableCommandsEvent event) {
        new PlayerAvailableCommands(event);
    }
}
