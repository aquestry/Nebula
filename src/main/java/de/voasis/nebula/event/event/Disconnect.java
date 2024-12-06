package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.BackendServer;
import de.voasis.nebula.Nebula;

public class Disconnect {
    public Disconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        Nebula.partyManager.quit(player);
        for(BackendServer backendServer : Data.backendInfoMap) {
            backendServer.removePendingPlayerConnection(player);
        }
    }
}