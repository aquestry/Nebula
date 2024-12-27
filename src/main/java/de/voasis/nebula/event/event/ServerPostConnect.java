package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Nebula;

public class ServerPostConnect {
    public ServerPostConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer server = player.getCurrentServer().get().getServer();
        Nebula.util.getBackendServer(server.getServerInfo().getName()).removePendingPlayerConnection(player);
        player.sendPlayerListHeader(Nebula.mm.deserialize("<blue><bold>Nebula Network</bold></blue>"));
        Nebula.permissionManager.sendRanktoBackend(player);
        Nebula.permissionManager.sendScoretoBackend(player);
    }
}