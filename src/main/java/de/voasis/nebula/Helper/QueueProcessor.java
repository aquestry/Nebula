package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
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
        for (GamemodeQueue queue : Data.gamemodeQueueMap) {
            int neededPlayers = queue.getNeededPlayers();
            if (queue.getInQueue().size() >= neededPlayers) {
                List<Player> playersToMove = new ArrayList<>();
                for (int i = 0; i < neededPlayers; i++) {
                    playersToMove.add(queue.getInQueue().getFirst());
                    queue.getInQueue().removeFirst();
                }
                BackendServer proccesServer = getServer(queue);
                for (Player player : playersToMove) {
                    proccesServer.addPendingPlayerConnection(player);
                }
                Nebula.util.callPending(proccesServer);
            }
        }
    }

    private BackendServer getServer(GamemodeQueue queue) {
        for(BackendServer backendServer : Data.backendInfoMap) {
            if(backendServer.getFlags().contains("gamemode:" + queue.getName()) && backendServer.getFlags().contains("preload") && backendServer.isOnline()) {
                backendServer.removeFlag("preload");
                createNew(queue).addFlag("preload");
                return backendServer;
            }
        }
        return createNew(queue);
    }

    public BackendServer createNew(GamemodeQueue queue) {
        String name = queue.getName() + "-" + Nebula.util.generateUniqueString();
        return Nebula.serverManager.createFromTemplate(
                queue.getTemplate(),
                name,
                server.getConsoleCommandSource(),
                "gamemode:" + queue.getName());
    }
}
