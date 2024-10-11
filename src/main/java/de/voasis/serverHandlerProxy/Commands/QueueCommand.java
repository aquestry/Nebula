package de.voasis.serverHandlerProxy.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.voasis.serverHandlerProxy.Helper.DataHolder;
import de.voasis.serverHandlerProxy.Maps.QueueInfo;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class QueueCommand implements SimpleCommand {

    private final DataHolder dataHolder;

    public QueueCommand(DataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player player)) {
            source.sendMessage(Component.text("Only players can use this command."));
            return;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
            for (QueueInfo q : dataHolder.queues) {
                if (q.getPlayers().contains(player)) {
                    q.removePlayer(player);
                    source.sendMessage(Component.text("You got removed from queue: " + q.getGamemode().getName()));
                    return;
                }
            }
            source.sendMessage(Component.text("You are in no queue."));
            return;
        }
        if (args.length != 2 || !args[0].equalsIgnoreCase("join") ) {
            source.sendMessage(Component.text("Usage: /queue join <queue name> Or: /queue leave"));
            return;
        }
        String queueName = args[1];
        Optional<QueueInfo> queueOptional = dataHolder.queues.stream()
                .filter(queue -> queue.getGamemode().getName().equalsIgnoreCase(queueName))
                .findFirst();

        if (queueOptional.isEmpty()) {
            source.sendMessage(Component.text("Queue not found: " + queueName));
            return;
        }

        QueueInfo queue = queueOptional.get();
        for (QueueInfo q : dataHolder.queues) {
            if (q.getPlayers().contains(player)) {
                source.sendMessage(Component.text("You are already in a queue."));
                return;
            }
        }

        queue.addPlayer(player);
        source.sendMessage(Component.text("You have successfully joined the queue: " + queueName));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 1) {
            return List.of("join","leave");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return dataHolder.queues.stream()
                    .map(queue -> queue.getGamemode().getName())
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
