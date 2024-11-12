package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.Component;

public class Disconnect {
    public Disconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        Nebula.util.leaveQueue(player);
        player.disconnect(Component.empty());
    }
}
