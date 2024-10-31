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
            boolean conditionsMet = backendServer.getTag().startsWith("gamemode:") &&
                    Nebula.util.getPlayerCount(backendServer) == 0 && backendServer.getPendingPlayerConnections().isEmpty();
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
}