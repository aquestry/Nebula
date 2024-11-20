package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;

public class Disconnect {
    public Disconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        Nebula.util.leaveQueue(player);
        for(BackendServer backendServer : Data.backendInfoMap) {
            backendServer.removePendingPlayerConnection(player);
        }
    }
}
