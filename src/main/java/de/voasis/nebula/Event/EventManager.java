package de.voasis.nebula.Event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Event.Events.*;
import de.voasis.nebula.Helper.DataHolder;
import de.voasis.nebula.Permission.PermissionManager;
import org.slf4j.Logger;

public class EventManager {

    Logger logger;
    ProxyServer server;
    DataHolder dataHolder;
    PermissionManager permissionManager;

    public EventManager(ProxyServer server, DataHolder dataHolder, Logger logger) {
        this.logger = logger;
        this.dataHolder = dataHolder;
        this.server = server;
    }
    @Subscribe
    public void onServerRegistered(ServerRegisteredEvent event) {
        new ServerRegistered(event, logger, server);
    }
    @Subscribe
    public void onServerUnregistered(ServerUnregisteredEvent event) {
        new ServerUnregistered(event, logger);
    }
    @Subscribe
    public void onChooseServer(PlayerChooseInitialServerEvent event) {
        new PlayerChooseInitialServer(event, logger);
    }
    @Subscribe
    public void preConnect(ServerPreConnectEvent event) {
        new ServerPreConnect(event);
    }
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        new ProxyShutdown(event, logger, server);
    }
    @Subscribe
    public void Perm(PermissionsSetupEvent event) {
        event.setProvider(permissionManager);
    }
}
