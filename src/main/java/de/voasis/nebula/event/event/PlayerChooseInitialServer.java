package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.map.BackendServer;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.Component;

public class PlayerChooseInitialServer {
    public PlayerChooseInitialServer(PlayerChooseInitialServerEvent event, ProxyServer server) {
        BackendServer target = Nebula.defaultsManager.getTarget();
        if(target != null) {
            event.setInitialServer(server.getServer(target.getServerName()).get());
        } else {
            event.getPlayer().disconnect(Component.text("Please reconnect!"));
            Nebula.defaultsManager.createDefault();
        }
    }
}
