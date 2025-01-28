package dev.aquestry.nebula.event.events;

import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.aquestry.nebula.Nebula;

public class ServerPostConnect {
    public ServerPostConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer server = player.getCurrentServer().get().getServer();
        Nebula.util.getBackendServer(server.getServerInfo().getName()).removePendingPlayerConnection(player);
        Nebula.util.sendInfotoBackend(player);
    }
}