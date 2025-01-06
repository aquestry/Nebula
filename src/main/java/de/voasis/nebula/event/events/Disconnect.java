package de.voasis.nebula.event.events;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Container;
import de.voasis.nebula.Nebula;

public class Disconnect {
    public Disconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        if(Nebula.partyManager.getParty(player) != null) {
            Nebula.partyManager.quit(player);
        } else {
            Nebula.queueProcessor.leaveQueue(player, false);
        }
        for(Container container : Config.containerMap) {
            container.removePendingPlayerConnection(player);
        }
    }
}