package de.voasis.nebula.helper;

import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Container;
import de.voasis.nebula.Nebula;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class AutoDeleter {

    private final Map<Container, Long> deletionTimers = new HashMap<>();
    private static final long DELETION_DELAY = 2000;

    public void process() {
        long currentTime = System.currentTimeMillis();
        List<Container> serversToDelete = new ArrayList<>();
        boolean lobbyServerDeleted = false;
        for (Container container : Data.backendInfoMap) {
            if (container.getFlags().contains("custom") || container.getFlags().contains("retry")) {
                continue;
            }
            boolean conditionsMet = Nebula.util.getPlayerCount(container) == 0 &&
                    container.getPendingPlayerConnections().isEmpty() && !container.getFlags().contains("preload");
            if (container.getFlags().contains("lobby")) {
                conditionsMet = conditionsMet && !lobbyServerDeleted && canDeleteLobbyServer(container);
            }
            if (conditionsMet) {
                if (!deletionTimers.containsKey(container)) {
                    deletionTimers.put(container, currentTime);
                } else {
                    long timerStarted = deletionTimers.get(container);
                    if (currentTime - timerStarted >= DELETION_DELAY) {
                        serversToDelete.add(container);
                        deletionTimers.remove(container);
                        if (container.getFlags().contains("lobby")) {
                            lobbyServerDeleted = true;
                        }
                    }
                }
            } else {
                deletionTimers.remove(container);
            }
        }
        for (Container serverToDelete : serversToDelete) {
            Nebula.serverManager.delete(serverToDelete, null);
        }
    }

    private boolean canDeleteLobbyServer(Container serverToExclude) {
        for (Container otherServer : Data.backendInfoMap) {
            if (!otherServer.equals(serverToExclude) && otherServer.getFlags().contains("lobby") && otherServer.isOnline() && Nebula.util.getPlayerCount(otherServer) < Data.defaultmin) {
                return true;
            }
        }
        return false;
    }
}