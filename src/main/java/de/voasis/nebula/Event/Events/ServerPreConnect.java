package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Nebula;

public class ServerPreConnect {
    public ServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer target = event.getOriginalServer();
        if (Nebula.util.getPlayerCount(target) >= Data.defaultmax || !Nebula.util.getBackendServer(target.getServerInfo().getName()).isOnline()) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
        if(player.getCurrentServer().isPresent()) {
            if(player.getCurrentServer().get().getServer().equals(target)) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
            }
        }
    }
}
