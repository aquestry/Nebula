package dev.aquestry.nebula.event.events;

import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.Nebula;

public class ServerPreConnect {
    public ServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer target = event.getOriginalServer();
        if (Nebula.util.getPlayerCount(target) >= Config.defaultmax || !Nebula.util.getBackendServer(target.getServerInfo().getName()).isOnline()) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
        if(target.getPlayersConnected().contains(player)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }
}