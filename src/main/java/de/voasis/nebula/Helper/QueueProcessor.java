package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Data.Util;
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

    public void process() {
        Data.gamemodeQueueMap.parallelStream().forEach(queue -> {
            if (queue.isPreload() && !Data.preloadedGameServers.containsKey(queue)) {
                Data.preloadedGameServers.put(queue, null);
                String name = queue.getName() + "-" + Util.generateUniqueString();
                BackendServer preloadedServer = Nebula.serverManager.createFromTemplate(
                        queue.getTemplate(),
                        name,
                        server.getConsoleCommandSource(),
                        "gamemode:" + queue.getName()
                );
                Data.preloadedGameServers.put(queue, preloadedServer);
            }
            int neededPlayers = queue.getNeededPlayers();
            if (queue.getInQueue().size() >= neededPlayers) {
                List<Player> playersToMove = new ArrayList<>();
                for (int i = 0; i < neededPlayers; i++) {
                    playersToMove.add(queue.getInQueue().getFirst());
                    queue.getInQueue().removeFirst();
                }
                BackendServer newServer;
                if (queue.isPreload()) newServer = Data.preloadedGameServers.get(queue);
                else {
                    String name = queue.getName() + "-" + Util.generateUniqueString();
                    newServer = Nebula.serverManager.createFromTemplate(
                            queue.getTemplate(),
                            name,
                            server.getConsoleCommandSource(),
                            "gamemode:" + queue.getName()
                    );
                }
                for (Player player : playersToMove) {
                    newServer.addPendingPlayerConnection(player);
                }
                if (queue.isPreload()) Data.preloadedGameServers.remove(queue);
            }
        });
    }
}
