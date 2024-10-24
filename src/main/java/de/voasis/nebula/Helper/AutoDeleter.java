package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
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
        Iterator<BackendServer> iterator = Nebula.dataHolder.backendInfoMap.iterator();
        while (iterator.hasNext()) {
            BackendServer backendServer = iterator.next();
            boolean conditionsMet = !Objects.equals(backendServer.getTag(), "default") && server.getServer(backendServer.getServerName()).get().getPlayersConnected().isEmpty();

            if (conditionsMet) {
                if (!deletionTimers.containsKey(backendServer)) {
                    deletionTimers.put(backendServer, currentTime);
                } else {
                    long timerStarted = deletionTimers.get(backendServer);
                    if (currentTime - timerStarted >= DELETION_DELAY) {
                        Nebula.serverManager.delete(backendServer, null);
                        deletionTimers.remove(backendServer);
                    }
                }
            } else {
                deletionTimers.remove(backendServer);
            }
        }
    }
}