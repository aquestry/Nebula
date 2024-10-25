package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.GamemodeQueue;
import de.voasis.nebula.Nebula;

public class QueueProcessor {

    public void process() {
        for(GamemodeQueue queue : Data.gamemodeQueueMap) {
            if(queue.getInQueue().size() >= queue.getNeededPlayers()) {
                Player player1 = queue.getInQueue().get(0);
                Player player2 = queue.getInQueue().get(1);
                queue.getInQueue().remove(0);
                queue.getInQueue().remove(0);
                BackendServer newServer = Nebula.serverManager.createFromTemplate(queue.getTemplate(), getName(queue), null, "gamemode:" + queue.getName());
                newServer.addPendingPlayerConnection(player1);
                newServer.addPendingPlayerConnection(player2);
            }
        }
    }

    public String getName(GamemodeQueue queue) {
        long count = Data.backendInfoMap.stream()
                .filter(backendServer -> backendServer.getTag().equals("gamemode:" + queue.getName()))
                .count();
        return queue.getName() + "-" + count;
    }
}
