package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.GamemodeQueue;
import de.voasis.nebula.Nebula;

public class QueueProcessor {

    private final ProxyServer server;

    public QueueProcessor(ProxyServer server) {
        this.server = server;
    }

    public void process() {
        for(GamemodeQueue queue : Data.gamemodeQueueMap) {
            if(queue.getInQueue().size() >= queue.getNeededPlayers()) {
                Player player1 = queue.getInQueue().get(0);
                Player player2 = queue.getInQueue().get(1);
                queue.getInQueue().remove(player1);
                queue.getInQueue().remove(player2);
                String name = queue.getName() + "-" + Nebula.util.generateUniqueString();
                BackendServer newServer = Nebula.serverManager.createFromTemplate(queue.getTemplate(), name, server.getConsoleCommandSource(), "gamemode:" + queue.getName());
                newServer.addPendingPlayerConnection(player1);
                newServer.addPendingPlayerConnection(player2);
            }
        }
    }
}
