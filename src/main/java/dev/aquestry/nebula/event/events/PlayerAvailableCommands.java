package dev.aquestry.nebula.event.events;

import com.mojang.brigadier.tree.RootCommandNode;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import dev.aquestry.nebula.Nebula;

public class PlayerAvailableCommands {
    public PlayerAvailableCommands(PlayerAvailableCommandsEvent event) {
        RootCommandNode<?> node = event.getRootNode();
        if(!Nebula.util.getBackendServer(event.getPlayer().getCurrentServer().get().getServerInfo().getName()).getFlags().contains("lobby")) {
            node.removeChildByName("queue");
        } else {
            node.removeChildByName("lobby");
        }
    }
}