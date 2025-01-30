package dev.aquestry.nebula.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.data.Messages;
import dev.aquestry.nebula.model.Queue;
import dev.aquestry.nebula.Nebula;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        if (invocation.source() instanceof Player player) {
            String[] args = invocation.arguments();
            List<String> suggestions = new ArrayList<>();
            boolean inQueue = Nebula.queueProcessor.isInAnyQueue(player);
            if(inQueue) {
                suggestions.add("leave");
            } else {
                suggestions.add("join");
            }
            if (args.length == 0) { return suggestions; }
            if (args.length == 1) {
                return suggestions.stream()
                        .filter(command -> command.startsWith(args[0].toLowerCase()))
                        .collect(Collectors.toList());
            }
            if (args.length == 2 && "join".equalsIgnoreCase(args[0]) && !inQueue) {
                return Config.queueMap.stream()
                        .map(Queue::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }
}