package de.voasis.nebula.Event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Event.Events.*;
import de.voasis.nebula.Helper.DataHolder;
import de.voasis.nebula.ExternalServerManager;
import de.voasis.nebula.Helper.Util;
import de.voasis.nebula.Permission.PermissionManager;
import org.slf4j.Logger;

public class EventManager {
    Logger logger;
    ProxyServer server;
    DataHolder dataHolder;
    PermissionManager permissionManager;
    ExternalServerManager externalServerManager;
    Util util;
    public EventManager(ProxyServer server, DataHolder dataHolder, Logger logger, ExternalServerManager externalServerManager, Util util, PermissionManager permissionManager) {
        this.logger = logger;
        this.dataHolder = dataHolder;
        this.server = server;
        this.permissionManager = permissionManager;
        this.externalServerManager = externalServerManager;
        this.util = util;
    }
    @Subscribe
    public void onServerRegistered(ServerRegisteredEvent event) {
        new ServerRegistered(event, dataHolder, logger, server);
    }
    @Subscribe
    public void onServerUnregistered(ServerUnregisteredEvent event) {
        new ServerUnregistered(event, dataHolder, logger);
    }
    @Subscribe
    public void onChooseServer(PlayerChooseInitialServerEvent event) {
        new PlayerChooseInitialServer(event, dataHolder, logger, util, externalServerManager);
    }
    @Subscribe
    public void preConnect(ServerPreConnectEvent event) {
        new ServerPreConnect(event, dataHolder);
    }
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        new ProxyShutdown(event, dataHolder, externalServerManager, logger, server);
    }
    @Subscribe
    public void Perm(PermissionsSetupEvent event) {
        event.setProvider(permissionManager);
    }
    @Subscribe
    public void onPlayerJoin(LoginEvent event) {
        new Login(event, dataHolder, permissionManager, logger);
    }
}
