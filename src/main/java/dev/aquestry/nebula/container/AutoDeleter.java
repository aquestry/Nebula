package dev.aquestry.nebula.container;

import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.model.Container;
import dev.aquestry.nebula.Nebula;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class AutoDeleter {

    private final Map<Container, Long> deletionTimers = new HashMap<>();
    private static final long DELETION_DELAY = 2000;

    public void process() {
        if(Config.quitting) return;
        long currentTime = System.currentTimeMillis();
        List<Container> serversToDelete = new ArrayList<>();
        boolean lobbyServerDeleted = false;
        for (Container container : Config.containerMap) {
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
        serversToDelete.forEach(container -> Nebula.containerManager.delete(container, null));
    }

    private boolean canDeleteLobbyServer(Container serverToExclude) {
        return Config.containerMap.stream().anyMatch(container -> !container.equals(serverToExclude)
                && container.getFlags().contains("lobby")
                && container.isOnline()
                && Nebula.util.getPlayerCount(container) < Config.defaultmin);
    }
}