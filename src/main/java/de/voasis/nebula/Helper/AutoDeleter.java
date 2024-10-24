package de.voasis.nebula.Helper;

import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import com.velocitypowered.api.proxy.ProxyServer;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AutoDeleter {
    private final Map<BackendServer, Long> deletionTimers = new HashMap<>();
    private static final long DELETION_DELAY = 3000;
    private final ProxyServer server;

    public AutoDeleter(ProxyServer server) {
        this.server = server;
    }

    public void process() {
        long currentTime = System.currentTimeMillis();
        List<BackendServer> serversToDelete = new ArrayList<>();

        for (BackendServer backendServer : Nebula.dataHolder.backendInfoMap) {
            boolean conditionsMet = !Objects.equals(backendServer.getTag(), "default") &&
                    server.getServer(backendServer.getServerName()).get().getPlayersConnected().isEmpty();

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