package de.voasis.nebula.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.data.Messages;
import de.voasis.nebula.map.Queue;
import de.voasis.nebula.Nebula;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueueCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (invocation.source() instanceof Player player) {
            if (args.length == 0) {
                Nebula.util.sendMessage(player, Messages.USAGE_QUEUE);
                return;
            }
            switch (args[0]) {
                case "leave" -> Nebula.queueProcessor.leaveQueue(player, true);
                case "join" -> {
                    if (args.length == 2) {
                        Nebula.queueProcessor.joinQueue(player, args[1]);
                    } else {
                        Nebula.util.sendMessage(player, Messages.USAGE_QUEUE);
                    }
                }
                default -> Nebula.util.sendMessage(player, Messages.USAGE_QUEUE);
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            return List.of("join", "leave");
        }
        if (args.length == 1) {
            return Stream.of("join", "leave")
                    .filter(command -> command.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && "join".equalsIgnoreCase(args[0])) {
            return Data.queueMap.stream()
                    .map(Queue::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}