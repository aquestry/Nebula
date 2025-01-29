package dev.aquestry.nebula.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.model.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ProxyCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length < 2) {
            return;
        }
        String action = args[0].toLowerCase();
        String proxyName = args[1];
        Proxy proxy = Config.proxyMap.stream()
                .filter(Proxy::isOnline)
                .filter(p -> p.getName().equalsIgnoreCase(proxyName))
                .findFirst()
                .orElse(null);
        if (proxy == null) {
            return;
        }
        switch (action) {
            case "nodes":
                Nebula.util.sendMessage(source, Nebula.multiProxySender.getNodes(proxy));
                break;
            case "containers":
                Nebula.util.sendMessage(source, Nebula.multiProxySender.getContainers(proxy));
                break;
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            return CompletableFuture.completedFuture(List.of("nodes", "containers"));
        }
        if (args.length == 1) {
            return CompletableFuture.completedFuture(
                    List.of("nodes", "containers").stream()
                            .filter(option -> option.startsWith(args[0].toLowerCase()))
                            .collect(Collectors.toList())
            );
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("nodes") || args[0].equalsIgnoreCase("containers"))) {
            return CompletableFuture.completedFuture(
                    Config.proxyMap.stream()
                            .filter(Proxy::isOnline)
                            .map(Proxy::getName)
                            .filter(name -> name.startsWith(args[1]))
                            .collect(Collectors.toList())
            );
        }
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(SimpleCommand.Invocation invocation) {
        CommandSource sender = invocation.source();
        return sender.hasPermission("velocity.admin") || sender instanceof ConsoleCommandSource;
    }
}