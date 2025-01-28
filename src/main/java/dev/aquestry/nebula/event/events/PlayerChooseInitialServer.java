package dev.aquestry.nebula.event.events;

import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import dev.aquestry.nebula.model.Container;
import dev.aquestry.nebula.Nebula;
import net.kyori.adventure.text.Component;

public class PlayerChooseInitialServer {
    public PlayerChooseInitialServer(PlayerChooseInitialServerEvent event) {
        Container target = Nebula.defaultsManager.getTarget();
        if(target != null) {
            event.setInitialServer(Nebula.server.getServer(target.getServerName()).get());
        } else {
            event.getPlayer().disconnect(Component.text("Please rejoin!"));
            Nebula.defaultsManager.createDefault();
        }
    }
}