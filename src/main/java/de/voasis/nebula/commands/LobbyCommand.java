package de.voasis.nebula.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;

public class LobbyCommand implements SimpleCommand {
    public void execute(SimpleCommand.Invocation invocation) {
        String[] args = invocation.arguments();
        if (invocation.source() instanceof Player player) {
            Nebula.util.connectPlayer(player, Nebula.defaultsManager.getTarget(), false);
        }
    }
}
