package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Container;
import de.voasis.nebula.Nebula;

public class Disconnect {
    public Disconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        if(Nebula.partyManager.getParty(player) != null) {
            Nebula.partyManager.quit(player);
        } else {
            Nebula.queueProcessor.leaveQueue(player, false);
        }
        for(Container container : Data.backendInfoMap) {
            container.removePendingPlayerConnection(player);
        }
    }
}