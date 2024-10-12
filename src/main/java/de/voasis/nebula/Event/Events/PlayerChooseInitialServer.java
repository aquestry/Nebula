package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Helper.DataHolder;
import de.voasis.nebula.Maps.BackendServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

public class PlayerChooseInitialServer {
    public PlayerChooseInitialServer(PlayerChooseInitialServerEvent event, DataHolder dataHolder, Logger logger) {
        Player player = event.getPlayer();
        RegisteredServer defaultServer = dataHolder.defaultRegisteredServer;
        if (defaultServer != null) {
            BackendServer info = dataHolder.getBackendServer(defaultServer.getServerInfo().getName());
            if(info != null) {
                if(info.isOnline()) {
                    event.setInitialServer(defaultServer);
                    logger.info("Default-Server is online, connecting player...");
                    return;
                }
            }
        }
        logger.info("Default-Server is offline, disconnecting player...");
        player.disconnect(Component.text("Default-Server is not online"));
        event.setInitialServer(null);
    }
}
