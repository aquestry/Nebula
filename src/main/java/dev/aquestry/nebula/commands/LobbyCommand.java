package dev.aquestry.nebula.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.aquestry.nebula.Nebula;

public class LobbyCommand implements SimpleCommand {
    public void execute(SimpleCommand.Invocation invocation) {
        String[] args = invocation.arguments();
        if (invocation.source() instanceof Player player) {
            if(!Nebula.util.getBackendServer(player.getCurrentServer().get().getServerInfo().getName()).getFlags().contains("lobby")) {
                Nebula.util.connectPlayer(player, Nebula.defaultsManager.getTarget(), false);
            }
        }
    }
}
