package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.HoldServer;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class AdminCommand implements SimpleCommand {

    private final Logger logger = LoggerFactory.getLogger("nebula");
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 1) {
            source.sendMessage(Component.text("Usage: /admin <stop|delete|template> <args...>", NamedTextColor.GOLD));
            return;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "kill":
                if (args.length < 2) {
                    source.sendMessage(Component.text("Usage: /admin stop <InstanceName>", NamedTextColor.GOLD));
                    return;
                }
                handleKillCommand(source, args);
                break;
            case "delete":
                if (args.length < 2) {
                    source.sendMessage(Component.text("Usage: /admin delete <InstanceName>", NamedTextColor.GOLD));
                    return;
                }
                handleDeleteCommand(source, args);
                break;
            case "template":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Usage: /admin template <templateName> <newName>", NamedTextColor.GOLD));
                    return;
                }
                handleTemplateCommand(source, args);
                break;
            default:
                source.sendMessage(Component.text("Unknown command. Usage: /admin <stop|delete|template> <args...>", NamedTextColor.GOLD));
        }
    }


    private void handleKillCommand(CommandSource source, String[] args) {
        BackendServer backendServer = Nebula.util.getBackendServer(args[1]);
        if (backendServer != null) {
            source.sendMessage(Component.text("Killing server instance...", NamedTextColor.AQUA));
            Nebula.serverManager.kill(backendServer, source);
        } else {
            source.sendMessage(Component.text("Server not found.", NamedTextColor.GOLD));
        }
    }

    private void handleDeleteCommand(CommandSource source, String[] args) {
        BackendServer backendServer = Nebula.util.getBackendServer(args[1]);
        if (backendServer != null) {
            source.sendMessage(Component.text("Deleting server instance...", NamedTextColor.AQUA));
            Nebula.serverManager.delete(backendServer, source);
        } else {
            source.sendMessage(Component.text("Server not found.", NamedTextColor.GOLD));
        }
    }

    private void handleTemplateCommand(CommandSource source, String[] args) {
        String templateName = args[1];
        String newName = args[2];
        if (Nebula.util.getBackendServer(newName) == null) {
            source.sendMessage(Component.text("Creating server instance from template...", NamedTextColor.AQUA));
            Nebula.serverManager.createFromTemplate(templateName, newName, source, "custom");
        } else {
            source.sendMessage(Component.text("Server with the specified name already exists.", NamedTextColor.GOLD));
        }
    }

    private HoldServer findServerInfo(String serverName) {
        for (HoldServer info : Data.holdServerMap) {
            if (serverName.equals(info.getServerName())) {
                return info;
            }
        }
        return null;
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

        if (args.length == 3 && "template".equalsIgnoreCase(args[0])) {
            return CompletableFuture.completedFuture(List.of());
        }

        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}