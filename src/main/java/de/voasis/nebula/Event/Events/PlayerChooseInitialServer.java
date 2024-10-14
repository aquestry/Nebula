package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;

public class PlayerChooseInitialServer {
    public PlayerChooseInitialServer(PlayerChooseInitialServerEvent event, Logger logger, ProxyServer server) {
        RegisteredServer registeredServer = Nebula.defaultManager.getDefault();
        logger.info("Choose server {} for player {}.", registeredServer.getServerInfo().getName(), event.getPlayer().getUsername());
        event.setInitialServer(registeredServer);
    }
}
