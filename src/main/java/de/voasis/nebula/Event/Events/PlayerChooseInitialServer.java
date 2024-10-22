package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class PlayerChooseInitialServer {
    public PlayerChooseInitialServer(PlayerChooseInitialServerEvent event, Logger logger) {
        Player player = event.getPlayer();
        RegisteredServer defaultServer = Nebula.defaultManager.getDefault();
        if (defaultServer != null) {
            event.setInitialServer(defaultServer);
            logger.info("Connecting player...");
            return;
        }
        logger.info("No server found, disconnecting player...");
        player.disconnect(Component.text("No server found!"));
        event.setInitialServer(null);
    }
}
