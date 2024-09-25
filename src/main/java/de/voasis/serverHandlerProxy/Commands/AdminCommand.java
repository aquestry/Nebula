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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            source.sendMessage(Component.text("Usage: /admin <start|stop|delete|template> <args...>", NamedTextColor.GOLD));
            return;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "start":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Usage: /admin start <externalServerName> <InstanceName>", NamedTextColor.GOLD));
                    return;
                }
                handleStartCommand(source, args);
                break;
            case "stop":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Usage: /admin stop <externalServerName> <InstanceName>", NamedTextColor.GOLD));
                    return;
                }
                handleStopCommand(source, args);
                break;
            case "delete":
                if (args.length < 3) {
                    source.sendMessage(Component.text("Usage: /admin delete <externalServerName> <InstanceName>", NamedTextColor.GOLD));
                    return;
                }
                handleDeleteCommand(source, args);
                break;
            case "template":
                if (args.length < 6) {
                    source.sendMessage(Component.text("Usage: /admin template <externalServerName> <templateName> <newName> \"<startCMD>\" \"<stopCMD>\"", NamedTextColor.GOLD));
                    return;
                }
                handleTemplateCommand(source, args);
                break;
            default:
                source.sendMessage(Component.text("Unknown command. Usage: /admin <start|stop|delete|template> <args...>", NamedTextColor.GOLD));
        }
    }

    private void handleStartCommand(CommandSource source, String[] args) {
        String externalServerName = args[1];
        String instanceName = args[2];
        ServerInfo temp = findServerInfo(externalServerName);

        if (temp != null) {
            ServerHandlerProxy.externalServerCreator.start(temp, instanceName);
            source.sendMessage(Component.text("Starting server instance...", NamedTextColor.AQUA));
        } else {
            source.sendMessage(Component.text("Server not found.", NamedTextColor.RED));
        }
    }

    private void handleStopCommand(CommandSource source, String[] args) {
        String externalServerName = args[1];
        String instanceName = args[2];
        ServerInfo temp = findServerInfo(externalServerName);

        if (temp != null) {
            ServerHandlerProxy.externalServerCreator.stop(temp, instanceName);
            source.sendMessage(Component.text("Stopping server instance...", NamedTextColor.AQUA));
        } else {
            source.sendMessage(Component.text("Server not found.", NamedTextColor.RED));
        }
    }

    private void handleDeleteCommand(CommandSource source, String[] args) {
        String externalServerName = args[1];
        String instanceName = args[2];
        ServerInfo temp = findServerInfo(externalServerName);

        if (temp != null) {
            ServerHandlerProxy.externalServerCreator.delete(temp, instanceName);
            source.sendMessage(Component.text("Deleting server instance...", NamedTextColor.AQUA));
        } else {
            source.sendMessage(Component.text("Server not found.", NamedTextColor.RED));
        }
    }

    private void handleTemplateCommand(CommandSource source, String[] args) {
        String externalServerName = args[1];
        String templateName = args[2];
        String newName = args[3];
        String startCMD = args[4];
        String stopCMD = args[5];
        ServerInfo temp = findServerInfo(externalServerName);

        if (temp != null) {
            int tempPort = extractPortFromStartCmd(startCMD);
            if (tempPort == -1) {
                source.sendMessage(Component.text("Invalid or missing port in startCMD: " + startCMD, NamedTextColor.RED));
                return;
            }
            ServerHandlerProxy.externalServerCreator.createFromTemplate(temp, templateName, newName, startCMD, stopCMD);
            source.sendMessage(Component.text("Creating server instance from template...", NamedTextColor.AQUA));
        } else {
            source.sendMessage(Component.text("Server not found.", NamedTextColor.RED));
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

    private int extractPortFromStartCmd(String startCMD) {
        Pattern pattern = Pattern.compile("-p\\s+(\\d+)");
        Matcher matcher = pattern.matcher(startCMD);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                logger.error("Error parsing port number: " + matcher.group(1), e);
            }
        }
        return -1;
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
        } else if (args.length == 3) {
            if (args[0].equals("start")) {
                return CompletableFuture.completedFuture(ServerHandlerProxy.dataHolder.backendInfoMap.stream()
                        .filter(server -> !server.getState())
                        .map(BackendServer::getServerName)
                        .toList());
            } else if (args[0].equals("stop")) {
                return CompletableFuture.completedFuture(ServerHandlerProxy.dataHolder.backendInfoMap.stream()
                        .filter(BackendServer::getState)
                        .map(BackendServer::getServerName)
                        .toList());
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