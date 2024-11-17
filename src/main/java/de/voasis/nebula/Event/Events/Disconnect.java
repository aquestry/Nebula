package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Disconnect {
    private final Logger logger = LoggerFactory.getLogger("nebula");
    public Disconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        Nebula.util.leaveQueue(player);
        for(BackendServer backendServer : Data.backendInfoMap) {
            backendServer.removePendingPlayerConnection(player);
        }
        logger.info("{} disconnected", player.getUsername());
    }
}
