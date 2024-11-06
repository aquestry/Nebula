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
        boolean lobbyServerDeleted = false;

        for (BackendServer backendServer : Data.backendInfoMap) {
            if (backendServer.getTag().equals("custom")) {
                continue;
            }

            boolean conditionsMet = Nebula.util.getPlayerCount(backendServer) == 0 &&
                    backendServer.getPendingPlayerConnections().isEmpty();

            if (backendServer.getTag().equals("lobby")) {
                conditionsMet = conditionsMet && !lobbyServerDeleted && canDeleteLobbyServer(backendServer);
            }

            if (conditionsMet) {
                if (!deletionTimers.containsKey(backendServer)) {
                    deletionTimers.put(backendServer, currentTime);
                } else {
                    long timerStarted = deletionTimers.get(backendServer);
                    if (currentTime - timerStarted >= DELETION_DELAY) {
                        serversToDelete.add(backendServer);
                        deletionTimers.remove(backendServer);
                        if (backendServer.getTag().equals("lobby")) {
                            lobbyServerDeleted = true;
                        }
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

    private int getActiveLobbyServerCount() {
        int count = 0;
        for (BackendServer server : Data.backendInfoMap) {
            if (server.getTag().equals("lobby") && server.isOnline()) {
                count++;
            }
        }
        return count;
    }

    private boolean canDeleteLobbyServer(BackendServer serverToExclude) {
        int activeLobbies = getActiveLobbyServerCount();
        if (activeLobbies <= 1) {
            return false;
        }
        for (BackendServer otherServer : Data.backendInfoMap) {
            if (!otherServer.equals(serverToExclude) && otherServer.getTag().equals("lobby") && otherServer.isOnline()) {
                int playerCount = Nebula.util.getPlayerCount(otherServer);
                if (playerCount < Data.defaultmin) {
                    return true;
                }
            }
        }
        return false;
    }
}
