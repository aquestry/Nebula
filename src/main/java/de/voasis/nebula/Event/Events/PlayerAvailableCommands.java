package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;

import java.util.Objects;

public class PlayerAvailableCommands {
    public PlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        Player player = event.getPlayer();
        if(!Objects.equals(Nebula.dataHolder.getBackendServer(player.getCurrentServer().get().getServerInfo().getName()).getTag(), "default")) {
            event.getRootNode().removeChildByName("queue");
        }
    }
}