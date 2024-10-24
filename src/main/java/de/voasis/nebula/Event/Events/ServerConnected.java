package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.player.ServerConnectedEvent;
import de.voasis.nebula.Nebula;

public class ServerConnected {
    public ServerConnected(ServerConnectedEvent event) {
        Nebula.dataHolder.getBackendServer(event.getServer().getServerInfo().getName()).removePendingPlayerConnection(event.getPlayer());
    }
}
