package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Maps.GamemodeQueue;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueueCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (invocation.source() instanceof Player player) {
            if (args.length == 0) {
                sendUsage(player);
                return;
            }
            switch (args[0]) {
                case "leave" -> leaveQueue(player);
                case "join" -> {
                    if (args.length == 2) {
                        joinQueue(player, args[1]);
                    } else {
                        sendUsage(player);
                    }
                }
                default -> sendUsage(player);
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
            return Nebula.dataHolder.gamemodeQueueMap.stream()
                    .map(GamemodeQueue::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }


    private void sendUsage(Player player) {
        player.sendMessage(Component.text("Usage: /queue leave or /queue join <name>", NamedTextColor.GOLD));
    }

    private boolean isInAnyQueue(Player player) {
        return Nebula.dataHolder.gamemodeQueueMap.stream()
                .anyMatch(queue -> queue.getInQueue().contains(player.getUniqueId().toString()));
    }

    private void joinQueue(Player player, String queueName) {
        if (isInAnyQueue(player)) {
            player.sendMessage(Component.text("You are already in a queue.", NamedTextColor.GOLD));
            return;
        }
        Nebula.dataHolder.gamemodeQueueMap.stream()
                .filter(queue -> queue.getName().equalsIgnoreCase(queueName))
                .findFirst()
                .ifPresentOrElse(
                        queue -> {
                            queue.addInQueue(player);
                            player.sendMessage(Component.text("You got added to queue: " + queueName + ".", NamedTextColor.GREEN));
                        },
                        () -> player.sendMessage(Component.text("Queue not found.", NamedTextColor.RED))
                );
    }

    private void leaveQueue(Player player) {
        if (!isInAnyQueue(player)) {
            player.sendMessage(Component.text("You are in no queue.", NamedTextColor.GOLD));
            return;
        }
        for(GamemodeQueue queue : Nebula.dataHolder.gamemodeQueueMap) {
            if (queue.getInQueue().contains(player.getUniqueId().toString())) {
                player.sendMessage(Component.text("You got removed from queue: " + queue.getName() + ".", NamedTextColor.GOLD));
                queue.removeInQueue(player);
            }
        }
    }
}
