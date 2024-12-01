package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import de.voasis.nebula.Nebula;

public class PlayerAvailableCommands {
    public PlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        if(!Nebula.util.getBackendServer(event.getPlayer().getCurrentServer().get().getServerInfo().getName()).getFlags().contains("lobby")) {
            event.getRootNode().removeChildByName("queue");
        }
    }
}