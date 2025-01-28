package dev.aquestry.nebula.event.events;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.model.Container;
import dev.aquestry.nebula.Nebula;

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