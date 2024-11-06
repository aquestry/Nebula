package de.voasis.nebula.Helper;

import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class AutoDeleter {

    private final Map<BackendServer, Long> deletionTimers = new HashMap<>();
    private static final long DELETION_DELAY = 2000;

    public void process() {
        long currentTime = System.currentTimeMillis();
        List<BackendServer> serversToDelete = new ArrayList<>();

        for (BackendServer backendServer : Data.backendInfoMap) {
            if (backendServer.getTag().equals("custom")) {
                continue;
            }

            boolean conditionsMet = Nebula.util.getPlayerCount(backendServer) == 0 &&
                    backendServer.getPendingPlayerConnections().isEmpty();

            if (backendServer.getTag().equals("lobby")) {
                conditionsMet = conditionsMet && hasSufficientLobbyServers(backendServer);
            }

            if (conditionsMet) {
                if (!deletionTimers.containsKey(backendServer)) {
                    deletionTimers.put(backendServer, currentTime);
                } else {
                    long timerStarted = deletionTimers.get(backendServer);
                    if (currentTime - timerStarted >= DELETION_DELAY) {
                        serversToDelete.add(backendServer);
                        deletionTimers.remove(backendServer);
                    }
                }
            } else {
                deletionTimers.remove(backendServer);
            }
        }

        for (BackendServer serverToDelete : serversToDelete) {
            Nebula.serverManager.delete(serverToDelete, null);
        }
    }

    private boolean hasSufficientLobbyServers(BackendServer serverToExclude) {
        int totalLobbies = 0;
        int lobbiesUnderMin = 0;

        for (BackendServer otherServer : Data.backendInfoMap) {
            if (otherServer.getTag().equals("lobby") && otherServer.isOnline()) {
                totalLobbies++;
                if (!otherServer.equals(serverToExclude) && Nebula.util.getPlayerCount(otherServer) < Data.defaultmin) {
                    lobbiesUnderMin++;
                }
            }
        }

        return totalLobbies > 1 && lobbiesUnderMin >= 1;
    }
}
