package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;

public class PlayerAvailableCommands {
    public PlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();
        if(!Nebula.util.getBackendServer(player.getCurrentServer().get().getServerInfo().getName()).getFlags().contains("lobby")) {
            event.getRootNode().removeChildByName("queue");
        }
    }
}
