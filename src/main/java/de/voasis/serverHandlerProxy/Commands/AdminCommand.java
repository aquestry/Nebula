package de.voasis.serverHandlerProxy.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import de.voasis.serverHandlerProxy.ServerHandlerProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdminCommand implements SimpleCommand {

    private Logger logger;
    public AdminCommand(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = parseQuotedArguments(invocation.arguments());

        if (args.length < 1) {
            source.sendMessage(Component.text("Usage: /admin <stop|delete|template> <args...>", NamedTextColor.GOLD));
            return;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "kill":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Usage: /admin stop <externalServerName> <InstanceName>", NamedTextColor.GOLD));
                    return;
                }
                handleKillCommand(source, args);
                break;
            case "delete":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Usage: /admin delete <externalServerName> <InstanceName>", NamedTextColor.GOLD));
                    return;
                }
                handleDeleteCommand(source, args);
                break;
            case "template":
                if (args.length < 4) {
                    source.sendMessage(Component.text("Usage: /admin template <externalServerName> <templateName> <newName>", NamedTextColor.GOLD));
                    return;
                }
                handleTemplateCommand(source, args);
                break;
            default:
                source.sendMessage(Component.text("Unknown command. Usage: /admin <stop|delete|template> <args...>", NamedTextColor.GOLD));
        }
    }


    private void handleKillCommand(CommandSource source, String[] args) {
        String externalServerName = args[1];
        String instanceName = args[2];
        ServerInfo temp = findServerInfo(externalServerName);

        if (temp != null && ServerHandlerProxy.dataHolder.getBackendServer(instanceName) != null) {
            source.sendMessage(Component.text("Killing server instance...", NamedTextColor.AQUA));
            ServerHandlerProxy.externalServerManager.kill(temp, instanceName, source);
        } else {
            source.sendMessage(Component.text("Server not found.", NamedTextColor.GOLD));
        }
    }

    private void handleDeleteCommand(CommandSource source, String[] args) {
        String externalServerName = args[1];
        String instanceName = args[2];
        ServerInfo temp = findServerInfo(externalServerName);

        if (temp != null && ServerHandlerProxy.dataHolder.getBackendServer(instanceName) != null) {
            source.sendMessage(Component.text("Deleting server instance...", NamedTextColor.AQUA));
            ServerHandlerProxy.externalServerManager.delete(temp, instanceName, source);
        } else {
            source.sendMessage(Component.text("Server not found.", NamedTextColor.GOLD));
        }
    }

    private void handleTemplateCommand(CommandSource source, String[] args) {
        String externalServerName = args[1];
        String templateName = args[2];
        String newName = args[3];
        ServerInfo temp = findServerInfo(externalServerName);
        if (temp != null) {
            if(ServerHandlerProxy.dataHolder.getBackendServer(newName) == null) {
                source.sendMessage(Component.text("Creating server instance from template...", NamedTextColor.AQUA));
                ServerHandlerProxy.externalServerManager.createFromTemplate(temp, templateName, newName, source);
            } else {
                source.sendMessage(Component.text("Server already exists.", NamedTextColor.GOLD));
            }
        } else {
            source.sendMessage(Component.text("Server not found.", NamedTextColor.GOLD));
        }
    }

    private ServerInfo findServerInfo(String serverName) {
        for (ServerInfo info : ServerHandlerProxy.dataHolder.serverInfoMap) {
            if (serverName.equals(info.getServerName())) {
                return info;
            }
        }
        return null;
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 1) {
            return CompletableFuture.completedFuture(List.of("kill", "delete", "template"));
        } else if (args.length == 2) {
            return CompletableFuture.completedFuture(ServerHandlerProxy.dataHolder.serverInfoMap.stream()
                    .map(ServerInfo::getServerName)
                    .toList());
        } else if (args.length == 3) {
            switch (args[0]) {
                case "kill" -> {
                    return CompletableFuture.completedFuture(ServerHandlerProxy.dataHolder.backendInfoMap.stream()
                            .filter(BackendServer::isOnline)
                            .filter(server -> server.getHoldServer().equals(args[1]))
                            .map(BackendServer::getServerName)
                            .toList());
                }
                case "delete" -> {
                    return CompletableFuture.completedFuture(ServerHandlerProxy.dataHolder.backendInfoMap.stream()
                            .filter(server -> server.getHoldServer().equals(args[1]))
                            .map(BackendServer::getServerName)
                            .toList());
                }
            }
        }

        return CompletableFuture.completedFuture(List.of());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }

    private String[] parseQuotedArguments(String[] args) {
        List<String> result = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean inQuotes = false;

        for (String arg : args) {
            if (arg.startsWith("\"") && arg.endsWith("\"")) {
                result.add(arg.substring(1, arg.length() - 1));
            } else if (arg.startsWith("\"")) {
                inQuotes = true;
                currentArg = new StringBuilder(arg.substring(1));
            } else if (arg.endsWith("\"") && inQuotes) {
                currentArg.append(" ").append(arg, 0, arg.length() - 1);
                result.add(currentArg.toString());
                inQuotes = false;
            } else if (inQuotes) {
                currentArg.append(" ").append(arg);
            } else {
                result.add(arg);
            }
        }
        if (inQuotes) {
            result.add(currentArg.toString());
        }
        return result.toArray(new String[0]);
    }
}