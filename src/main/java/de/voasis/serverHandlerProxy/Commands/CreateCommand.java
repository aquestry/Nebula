package de.voasis.serverHandlerProxy.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import java.util.concurrent.CompletableFuture;
import java.util.List;

import com.velocitypowered.api.proxy.ConsoleCommandSource;
import de.voasis.serverHandlerProxy.ServerHandlerProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
public class CreateCommand implements SimpleCommand {
    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 3) {
            source.sendMessage(Component.text("Usage: /create <newName> <startCMD> <stopCMD>", NamedTextColor.RED));
            return;
        }

        String newName = args[0];
        String startCMD = args[1].replace("_", " ");
        String stopCMD = args[2].replace("_", " ");

        if (source instanceof ConsoleCommandSource) {
            ServerHandlerProxy.externalServerCreator.create(ServerHandlerProxy.dataHolder.getAllInfos().getFirst(), newName, startCMD, stopCMD);
            source.sendMessage(Component.text("Creating server instance...", NamedTextColor.AQUA));
        } else {
            source.sendMessage(Component.text("This command can only be executed by the console.", NamedTextColor.RED));
        }
    }


    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("command.test");
    }

    @Override
    public List<String> suggest(final Invocation invocation) {
        return List.of();
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(List.of());
    }
}
