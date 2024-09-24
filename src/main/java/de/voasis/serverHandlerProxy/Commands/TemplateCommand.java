package de.voasis.serverHandlerProxy.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
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

        if (args.length != 5) {
            source.sendMessage(Component.text("Usage: /template <externalServerName> <templateName> <newName> <startCMD> <stopCMD>", NamedTextColor.RED));
            return;
        }
        String externalServerName = args[0];
        String templateName = args[1];
        String newName = args[2];
        String startCMD = args[3].replace("_", " ");
        String stopCMD = args[4].replace("_", " ");
        ServerInfo temp = null;
        for (ServerInfo i : ServerHandlerProxy.dataHolder.getAllInfos()) {
            if(externalServerName.equals(i.getServerName())) {
                temp = i;
            }
        }
        if (source instanceof ConsoleCommandSource && temp != null || hasPermission(invocation)) {
            ServerHandlerProxy.externalServerCreator.createFromTemplate(temp,templateName, newName, startCMD, stopCMD);
            source.sendMessage(Component.text("Creating server instance from template...", NamedTextColor.AQUA));

        } else {
            source.sendMessage(Component.text("This command can only be executed by the console.", NamedTextColor.RED));
        }
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        if(invocation.source() instanceof Player player) {
            return ServerHandlerProxy.dataHolder.admins.contains(player.getUniqueId());
        } else {
            return true;
        }
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
