package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AdminCommand implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length == 0 || args.length < 2) {
            Nebula.util.sendMessage(source, Messages.USAGE_ADMIN);
            return;
        }
        switch (args[0].toLowerCase()) {
            case "kill":
                handleKillCommand(source, args);
                break;
            case "start":
                handleStartCommand(source, args);
                break;
            case "delete":
                handleDeleteCommand(source, args);
                break;
            case "template":
                if (args.length < 3) {
                    Nebula.util.sendMessage(source, Messages.USAGE_ADMIN);
                    return;
                }
                handleTemplateCommand(source, args);
                break;
            default:
                Nebula.util.sendMessage(source, Messages.USAGE_ADMIN);
                break;
        }
    }

    private void handleKillCommand(CommandSource source, String[] args) {
        BackendServer backendServer = Nebula.util.getBackendServer(args[1]);
        if(backendServer == null) {
            Nebula.util.sendMessage(source, Messages.SERVER_NOT_FOUND.replace("<name>", args[1]));
        } else {
            Nebula.serverManager.kill(backendServer, source);
        }
    }

    private void handleStartCommand(CommandSource source, String[] args) {
        BackendServer backendServer = Nebula.util.getBackendServer(args[1]);
        if(backendServer == null) {
            Nebula.util.sendMessage(source, Messages.SERVER_NOT_FOUND.replace("<name>", args[1]));
        } else {
            Nebula.serverManager.start(backendServer, source);
        }
    }

    private void handleDeleteCommand(CommandSource source, String[] args) {
        BackendServer backendServer = Nebula.util.getBackendServer(args[1]);
        if(backendServer == null) {
            Nebula.util.sendMessage(source, Messages.SERVER_NOT_FOUND.replace("<name>", args[1]));
        } else {
            Nebula.serverManager.delete(backendServer, source);
        }
    }

    private void handleTemplateCommand(CommandSource source, String[] args) {
        Nebula.serverManager.createFromTemplate(args[1], args[2], source, "custom");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            return CompletableFuture.completedFuture(List.of("template", "delete", "kill", "start"));
        }
        if (args.length == 1) {
            return CompletableFuture.completedFuture(Stream.of("template", "delete", "kill", "start")
                    .filter(command -> command.startsWith(args[0].toLowerCase()))
                    .toList());
        }
        if (args.length == 2) {
            if ("template".equalsIgnoreCase(args[0])) {
                return CompletableFuture.completedFuture(Data.alltemplates.stream()
                        .filter(template -> template.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList());
            } else {
                return CompletableFuture.completedFuture(Data.backendInfoMap.stream()
                        .map(BackendServer::getServerName)
                        .filter(serverName -> serverName.startsWith(args[1]))
                        .toList());
            }
        }
        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}