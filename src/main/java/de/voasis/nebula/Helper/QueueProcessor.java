package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.GamemodeQueue;
import de.voasis.nebula.Nebula;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueueProcessor {

    private final ProxyServer server;

    public QueueProcessor(ProxyServer server) {
        this.server = server;
    }

    public void init() {
        for (GamemodeQueue queue : Data.gamemodeQueueMap) {
            int preloadCount = queue.getPreload();
            for (int i = 0; i < preloadCount; i++) {
                createPreloadedServer(queue);
            }
        }
    }

    public void process() {
        for (GamemodeQueue queue : Data.gamemodeQueueMap) {
            int neededPlayers = queue.getNeededPlayers();
            if (queue.getInQueue().size() >= neededPlayers) {
                List<Player> playersToMove = new ArrayList<>();
                for (int i = 0; i < neededPlayers; i++) {
                    playersToMove.add(queue.getInQueue().removeFirst());
                }
                Optional<BackendServer> preloadedServer = findPreloadedServer(queue);
                if (preloadedServer.isPresent()) {
                    BackendServer server = preloadedServer.get();
                    server.removeFlag("preload");
                    for (Player player : playersToMove) {
                        Nebula.util.connectPlayer(player, server, false);
                    }
                    createPreloadedServer(queue);
                } else {
                    BackendServer newServer = createNewServer(queue);
                    for (Player player : playersToMove) {
                        newServer.addPendingPlayerConnection(player);
                    }
                    Nebula.util.callPending(newServer);
                }
            }
        }
    }

    private Optional<BackendServer> findPreloadedServer(GamemodeQueue queue) {
        return Data.backendInfoMap.stream()
                .filter(server -> {
                    boolean hasGamemode = server.getFlags().contains("gamemode:" + queue.getName());
                    boolean hasPreload = server.getFlags().contains("preload");
                    boolean isOnline = server.isOnline();
                    return hasGamemode && hasPreload && isOnline;
                })
                .findFirst();
    }

    private void createPreloadedServer(GamemodeQueue queue) {
        createNewServer(queue, "preload", "retry", "gamemode:" + queue.getName());
    }

    private BackendServer createNewServer(GamemodeQueue queue, String... flags) {
        String serverName = queue.getName() + "-" + Nebula.util.generateUniqueString();
        return Nebula.serverManager.createFromTemplate(
                queue.getTemplate(),
                serverName,
                server.getConsoleCommandSource(),
                flags
        );
    }

    public void joinQueue(Player player, String queueName) {
        BackendServer currentServer = Nebula.util.getBackendServer(
                player.getCurrentServer().get().getServerInfo().getName()
        );
        if (!currentServer.getFlags().contains("lobby")) {
            Nebula.util.sendMessage(player, Messages.LOBBY_ONLY);
            return;
        }
        if (isInAnyQueue(player)) {
            Nebula.util.sendMessage(player, Messages.ALREADY_IN_QUEUE);
            return;
        }
        Data.gamemodeQueueMap.stream()
                .filter(queue -> queue.getName().equalsIgnoreCase(queueName))
                .findFirst()
                .ifPresentOrElse(
                        queue -> {
                            queue.getInQueue().add(player);
                            Nebula.util.sendMessage(player, Messages.ADDED_TO_QUEUE.replace("<queue>", queueName));
                        },
                        () -> Nebula.util.sendMessage(player, Messages.QUEUE_NOT_FOUND)
                );
    }

    public void leaveQueue(Player player) {
        if (!isInAnyQueue(player)) {
            Nebula.util.sendMessage(player, Messages.NOT_IN_QUEUE);
            return;
        }
        for (GamemodeQueue queue : Data.gamemodeQueueMap) {
            if (queue.getInQueue().remove(player)) {
                Nebula.util.sendMessage(player, Messages.REMOVED_FROM_QUEUE.replace("<queue>", queue.getName()));
            }
        }
    }

    public boolean isInAnyQueue(Player player) {
        return Data.gamemodeQueueMap.stream()
                .anyMatch(queue -> queue.getInQueue().contains(player));
    }
}