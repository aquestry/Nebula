package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import java.util.Objects;

public class CommandExecute {
    public CommandExecute(CommandExecuteEvent event) {
        if(event.getCommandSource() instanceof Player player) {
            if(!Objects.equals(Nebula.dataHolder.getBackendServer(player.getCurrentServer().get().getServerInfo().getName()).getTag(), "default") && event.getCommand().startsWith("queue")) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());
            }
        }
    }
}
