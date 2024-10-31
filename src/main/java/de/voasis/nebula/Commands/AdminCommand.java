package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AdminCommand implements SimpleCommand {

    private MiniMessage mm = MiniMessage.miniMessage();
    private final Logger logger = LoggerFactory.getLogger("nebula");

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        switch (args[0].toLowerCase()) {
            case "kill":
                if (args.length < 2) {
                    source.sendMessage(mm.deserialize(Messages.USAGE_ADMIN));
                    return;
                }
                handleKillCommand(source, args);
                break;
            case "delete":
                if (args.length < 2) {
                    source.sendMessage(mm.deserialize(Messages.USAGE_ADMIN));
                    return;
                }
                handleDeleteCommand(source, args);
                break;
            case "template":
                if (args.length < 3) {
                    source.sendMessage(mm.deserialize(Messages.USAGE_ADMIN));
                    return;
                }
                handleTemplateCommand(source, args);
                break;
            default:
                source.sendMessage(mm.deserialize(Messages.USAGE_ADMIN));
        }
    }

    private void handleKillCommand(CommandSource source, String[] args) {
        BackendServer backendServer = Nebula.util.getBackendServer(args[1]);
        if (backendServer != null) {
            source.sendMessage(mm.deserialize(Messages.KILL_CONTAINER.replace("<name>", backendServer.getServerName())));
            Nebula.serverManager.kill(backendServer, source);
        } else {
            source.sendMessage(mm.deserialize(Messages.SERVER_NOT_FOUND.replace("<name>", backendServer.getServerName())));
        }
    }

    private void handleDeleteCommand(CommandSource source, String[] args) {
        BackendServer backendServer = Nebula.util.getBackendServer(args[1]);
        if (backendServer != null) {
            source.sendMessage(mm.deserialize(Messages.DELETE_CONTAINER.replace("<name>", backendServer.getServerName())));
            Nebula.serverManager.delete(backendServer, source);
        } else {
            source.sendMessage(mm.deserialize(Messages.SERVER_NOT_FOUND.replace("<name>", backendServer.getServerName())));
        }
    }

    private void handleTemplateCommand(CommandSource source, String[] args) {
        String templateName = args[1];
        String newName = args[2];
        Nebula.serverManager.createFromTemplate(templateName, newName, source, "custom");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 0) {
            return CompletableFuture.completedFuture(List.of("kill", "delete", "template"));
        }
        if (args.length == 1) {
            return CompletableFuture.completedFuture(Stream.of("kill", "delete", "template")
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