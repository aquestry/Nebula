package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ServerCommand implements SimpleCommand {

    private final ProxyServer server;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Logger logger = LoggerFactory.getLogger("nebula");

    public ServerCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (source instanceof Player player) {
            if(args.length != 0) {
                Optional<ServerConnection> currentServer = player.getCurrentServer();
                String targetServerName = args[0];
                if (currentServer.isPresent() && currentServer.get().getServerInfo().getName().equalsIgnoreCase(targetServerName)) {
                    player.sendMessage(mm.deserialize("<yellow>You are already connected to the target server!"));
                    return;
                }
                server.getServer(targetServerName).ifPresentOrElse(targetServer -> player.createConnectionRequest(targetServer).connect().thenAccept(result -> {
                    if (result.isSuccessful()) {
                        player.sendMessage(mm.deserialize("<green>Successfully connected to the target server!"));
                    } else {
                        player.sendMessage(mm.deserialize("<red>Failed to connect to the target server."));
                    }
                }), () -> {
                    player.sendMessage(mm.deserialize("<red>Target server not found!"));
                    logger.warn("Target server {} not found for player {}", targetServerName, player.getUsername());
                });
            } else {
                String serverList = Data.backendInfoMap.stream()
                        .map(backendServer -> {
                            int playerCount = Nebula.util.getPlayerCount(backendServer);
                            String status = backendServer.isOnline() ? "<green>Online" : "<red>Offline";
                            return "<hover:show_text:'<gray>Click to connect\n<yellow>Players: " + playerCount + "\nStatus: " + status + "'>"
                                    + "<click:run_command:'/server " + backendServer.getServerName() + "'><green>"
                                    + backendServer.getServerName() + "</click></hover>";
                        })
                        .reduce((s1, s2) -> s1 + ", " + s2)
                        .orElse("<red>No servers available.");
                player.sendMessage(mm.deserialize("<yellow>Available servers: </yellow>" + serverList));
            }
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        String[] args = invocation.arguments();
        return CompletableFuture.completedFuture(Data.backendInfoMap.stream()
                .map(BackendServer::getServerName)
                .filter(serverName -> args.length == 0 || serverName.startsWith(args[0]))
                .toList());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}
