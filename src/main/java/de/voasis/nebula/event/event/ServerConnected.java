package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.player.ServerConnectedEvent;
import de.voasis.nebula.Nebula;

public class ServerConnected {
    public ServerConnected(ServerConnectedEvent event) {
        Nebula.util.getBackendServer(event.getServer().getServerInfo().getName()).removePendingPlayerConnection(event.getPlayer());
    }
}