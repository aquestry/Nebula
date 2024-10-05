package de.voasis.serverHandlerProxy.Events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.permission.PermissionsSetupEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.voasis.serverHandlerProxy.Helper.DataHolder;
import de.voasis.serverHandlerProxy.ExternalServerManager;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Maps.Messages;
import de.voasis.serverHandlerProxy.Permission.PermissionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

public class EventManager {
    Logger logger;
    ProxyServer server;
    DataHolder dataHolder;
    PermissionManager permissionManager;
    ExternalServerManager externalServerManager;
    public EventManager(ProxyServer server, DataHolder dataHolder, Logger logger, ExternalServerManager externalServerManager, PermissionManager permissionManager) {
        this.logger = logger;
        this.dataHolder = dataHolder;
        this.server = server;
        this.permissionManager = permissionManager;
        this.externalServerManager = externalServerManager;
    }
    @Subscribe
    public void onServerRegistered(ServerRegisteredEvent event) {
        RegisteredServer reg = event.registeredServer();
        ServerInfo info = reg.getServerInfo();
        if (reg.getServerInfo().getName().equals(dataHolder.defaultServer)) {
            logger.info("Default-Server registered.");
            dataHolder.defaultRegisteredServer = server.registerServer(reg.getServerInfo());
        }
        logger.info("Server registered: " + reg.getServerInfo().getName() + ", IP: " + reg.getServerInfo().getAddress());

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
            player.sendMessage(Component.text(Messages.offline, NamedTextColor.GOLD));
        }
        if(player.getCurrentServer().equals(target)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            player.sendMessage(Component.text(Messages.already, NamedTextColor.GOLD));
        }
    }
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {deleteDefaultServer();}
    @Subscribe
    public void Perm(PermissionsSetupEvent event) {event.setProvider(permissionManager);}
    @Subscribe
    public void onPlayerJoin(LoginEvent event) {
        Player player = event.getPlayer();
        if(dataHolder.admins.contains(player.getUniqueId().toString())) {
            for(String perm : Messages.adminRights) {
                permissionManager.addPermission(player, perm);
            }
        }
        logger.info("Player is admin: " + player.hasPermission("velocity.admin"));
    }
    private void deleteDefaultServer() {
        logger.info("Deleting Default-Server...");
        externalServerManager.delete(dataHolder.serverInfoMap.getFirst(), dataHolder.defaultServer, null);
    }
}
