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
                BackendServer preloadedServer = queue.createServer(server);
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
                else newServer = queue.createServer(server);
                for (Player player : playersToMove) {
                    newServer.addPendingPlayerConnection(player);
                }
                if (queue.isPreload()) Data.preloadedGameServers.remove(queue);
            }
        });
    }
}
