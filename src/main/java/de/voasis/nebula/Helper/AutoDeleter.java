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
            if (backendServer.getFlags().contains("custom")) {
                continue;
            }
            boolean conditionsMet = Nebula.util.getPlayerCount(backendServer) == 0 &&
                    backendServer.getPendingPlayerConnections().isEmpty() && !backendServer.getFlags().contains("preload");
            if (backendServer.getFlags().contains("lobby")) {
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
                        if (backendServer.getFlags().contains("lobby")) {
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

    private boolean canDeleteLobbyServer(BackendServer serverToExclude) {
        for (BackendServer otherServer : Data.backendInfoMap) {
            if (!otherServer.equals(serverToExclude) && otherServer.getFlags().contains("lobby") && otherServer.isOnline() && Nebula.util.getPlayerCount(otherServer) < Data.defaultmin) {
                return true;
            }
        }
        return false;
    }
}
