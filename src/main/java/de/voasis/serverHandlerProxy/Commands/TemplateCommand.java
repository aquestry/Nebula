package de.voasis.serverHandlerProxy.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import de.voasis.serverHandlerProxy.ServerHandlerProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TemplateCommand implements SimpleCommand {
    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 3) {
            source.sendMessage(Component.text("Usage: /template <templateName> <newName> <startCMD> <stopCMD>", NamedTextColor.RED));
            return;
        }
        String templateName = args[0];
        String newName = args[1];
        String startCMD = args[2].replace("_", " ");
        String stopCMD = args[3].replace("_", " ");

        if (source instanceof ConsoleCommandSource) {
            ServerHandlerProxy.externalServerCreator.createFromTemplate(ServerHandlerProxy.dataHolder.getAllInfos().getFirst(),templateName, newName, startCMD, stopCMD);
            source.sendMessage(Component.text("Creating server instance from template...", NamedTextColor.AQUA));
        } else {
            source.sendMessage(Component.text("This command can only be executed by the console.", NamedTextColor.RED));
        }
    }


    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source().hasPermission("command.create");
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
