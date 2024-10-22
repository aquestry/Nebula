package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;

public class PlayerChooseInitialServer {
    public PlayerChooseInitialServer(PlayerChooseInitialServerEvent event, ProxyServer server) {
        BackendServer target = Nebula.defaultsManager.getTarget();
        event.setInitialServer(server.getServer(target.getServerName()).get());
    }
}
