package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;

import java.util.ArrayList;
import java.util.List;

public class QueueProcessor {

    private final ProxyServer server;

    public QueueProcessor(ProxyServer server) {
        this.server = server;
    }

    public void process() {
        Data.gamemodeQueueMap.parallelStream().forEach(queue -> {
            if (queue.getPreload() > 0) {
                if (!Data.preloadedGameServers.containsKey(queue)) Data.preloadedGameServers.put(queue, new ArrayList<>());
                List<BackendServer> queuePreloadedServer = Data.preloadedGameServers.get(queue);
                if (queuePreloadedServer.size() < queue.getPreload()) {
                    int i = queuePreloadedServer.size();
                    queuePreloadedServer.add(null);
                    BackendServer preloadedServer = queue.createServer(server);
                    if (preloadedServer == null) queuePreloadedServer.remove(i);
                    else queuePreloadedServer.set(i, preloadedServer);
                }
            }
            int neededPlayers = queue.getNeededPlayers();
            if (queue.getInQueue().size() >= neededPlayers) {
                List<Player> playersToMove = new ArrayList<>();
                for (int i = 0; i < neededPlayers; i++) {
                    playersToMove.add(queue.getInQueue().getFirst());
                    queue.getInQueue().removeFirst();
                }
                BackendServer newServer;
                if (queue.getPreload() > 0) newServer = Data.preloadedGameServers.get(queue).getFirst();
                else newServer = queue.createServer(server);
                for (Player player : playersToMove) {
                    newServer.addPendingPlayerConnection(player);
                }
                if (queue.getPreload() > 0) Data.preloadedGameServers.get(queue).remove(newServer);
            }
        });
    }
}
