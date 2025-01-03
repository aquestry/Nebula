package de.voasis.nebula.event.events;

import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.Nebula;

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