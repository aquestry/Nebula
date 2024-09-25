package de.voasis.serverHandlerProxy.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.voasis.serverHandlerProxy.Commands.SubCommands.DeleteCommand;
import de.voasis.serverHandlerProxy.Commands.SubCommands.StartCommand;
import de.voasis.serverHandlerProxy.Commands.SubCommands.StopCommand;
import de.voasis.serverHandlerProxy.Commands.SubCommands.TemplateCommand;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import de.voasis.serverHandlerProxy.ServerHandlerProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminCommand implements SimpleCommand {

    private final StartCommand startCommand = new StartCommand();
    private final StopCommand stopCommand = new StopCommand();
    private final DeleteCommand deleteCommand = new DeleteCommand();
    private final TemplateCommand templateCommand = new TemplateCommand();

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 1) {
            source.sendMessage(Component.text("Usage: /admin <start|stop|delete|template> <args...>", NamedTextColor.RED));
            return;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "start":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Usage: /admin start <externalServerName> <InstanceName>", NamedTextColor.RED));
                    return;
                }
                startCommand.execute(invocation);
                break;

            case "stop":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Usage: /admin stop <externalServerName> <InstanceName>", NamedTextColor.RED));
                    return;
                }
                stopCommand.execute(invocation);
                break;

            case "delete":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Usage: /admin delete <externalServerName> <InstanceName>", NamedTextColor.RED));
                    return;
                }
                deleteCommand.execute(invocation);
                break;

            case "template":
                if (args.length < 6) {
                    source.sendMessage(Component.text("Usage: /admin template <externalServerName> <templateName> <newName> <startCMD> <stopCMD>", NamedTextColor.RED));
                    return;
                }
                templateCommand.execute(invocation);
                break;

            default:
                source.sendMessage(Component.text("Unknown command. Usage: /admin <start|stop|delete|template> <args...>", NamedTextColor.RED));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 1) {
            return CompletableFuture.completedFuture(List.of("start", "stop", "delete", "template"));
        } else if (args.length == 2) {
            return CompletableFuture.completedFuture(ServerHandlerProxy.dataHolder.serverInfoMap.stream()
                    .map(ServerInfo::getServerName)
                    .toList());
        }

        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}
