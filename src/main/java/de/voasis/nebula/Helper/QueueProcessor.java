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

public class QueueProcessor {

    private final ProxyServer server;

    public QueueProcessor(ProxyServer server) {
        this.server = server;
    }

    public void init() {
        for (GamemodeQueue queue : Data.gamemodeQueueMap) {
            int preloadCount = queue.getPreload();
            for (int i = 0; i < preloadCount; i++) {
                createNew(queue, "preload", "retry");
            }
        }
    }

    public void process() {
        for (GamemodeQueue queue : Data.gamemodeQueueMap) {
            int neededPlayers = queue.getNeededPlayers();
            if (queue.getInQueue().size() >= neededPlayers) {
                List<Player> playersToMove = new ArrayList<>();
                for (int i = 0; i < neededPlayers; i++) {
                    playersToMove.add(queue.getInQueue().getFirst());
                    queue.getInQueue().removeFirst();
                }
                BackendServer processServer = getServer(queue);
                for (Player player : playersToMove) {
                    processServer.addPendingPlayerConnection(player);
                }
                if(processServer.getFlags().contains("preload")) {
                    Nebula.util.callPending(processServer);
                }
            }
        }
    }

    private BackendServer getServer(GamemodeQueue queue) {
        for (BackendServer backendServer : Data.backendInfoMap) {
            if (backendServer.getFlags().contains("gamemode:" + queue.getName()) &&
                    backendServer.getFlags().contains("preload") &&
                    backendServer.isOnline()) {
                backendServer.removeFlag("preload");
                createNew(queue, "preload");
                return backendServer;
            }
        }
        return createNew(queue);
    }

    public BackendServer createNew(GamemodeQueue queue, String... flags) {
        String name = queue.getName() + "-" + Nebula.util.generateUniqueString();
        List<String> allFlags = new ArrayList<>(List.of(flags));
        if (allFlags.contains("preload") && !allFlags.contains("retry")) {
            allFlags.add("retry");
        }
        return Nebula.serverManager.createFromTemplate(
                queue.getTemplate(),
                name,
                server.getConsoleCommandSource(),
                allFlags.toArray(new String[0])
        );
    }

    public boolean isInAnyQueue(Player player) {
        return Data.gamemodeQueueMap.stream().anyMatch(queue -> queue.getInQueue().contains(player));
    }

    public void joinQueue(Player player, String queueName) {
        if (!Nebula.util.getBackendServer(player.getCurrentServer().get().getServerInfo().getName()).getFlags().contains("lobby")) {
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
                        () -> Nebula.util.sendMessage(player, Messages.QUEUE_NOT_FOUND));
    }

    public void leaveQueue(Player player) {
        if (!isInAnyQueue(player)) {
            Nebula.util.sendMessage(player, Messages.NOT_IN_QUEUE);
            return;
        }
        for (GamemodeQueue queue : Data.gamemodeQueueMap) {
            if (queue.getInQueue().contains(player)) {
                Nebula.util.sendMessage(player, Messages.REMOVED_FROM_QUEUE.replace("<queue>", queue.getName()));
                queue.getInQueue().remove(player);
            }
        }
    }
}