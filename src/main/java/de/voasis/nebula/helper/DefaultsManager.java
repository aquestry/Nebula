package de.voasis.nebula.helper;

import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.BackendServer;
import de.voasis.nebula.Nebula;
import java.util.ArrayList;
import java.util.List;

public class DefaultsManager {
    public BackendServer getTarget() {
        BackendServer target = getServerWithLowestPlayerCount();
        if(target != null) {
            int count = Nebula.server.getServer(target.getServerName()).get().getPlayersConnected().size();
            if(count + 1 == Data.defaultmin && !isOtherUnderMin(target)) {
                createDefault();
            }
        }
        target = getServerBetweenMinAndMaxPlayers();
        if(target != null) {
            return target;
        }
        return getServerWithLowestPlayerCount();
    }

    private List<BackendServer> getAvailableServers() {
        List<BackendServer> servers = new ArrayList<>();
        for (BackendServer server : Data.backendInfoMap) {
            if (server.getFlags().contains("lobby") && server.isOnline()) {
                servers.add(server);
            }
        }
        return servers;
    }

    private BackendServer getServerWithLowestPlayerCount() {
        if (getAvailableServers().isEmpty()) {
            return null;
        }
        BackendServer serverWithLowestCount = getAvailableServers().getFirst();
        int lowestPlayerCount = Nebula.server.getServer(serverWithLowestCount.getServerName())
                .get()
                .getPlayersConnected()
                .size();
        for (BackendServer backendServer : getAvailableServers()) {
            int playerCount = Nebula.server.getServer(backendServer.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount < lowestPlayerCount) {
                serverWithLowestCount = backendServer;
                lowestPlayerCount = playerCount;
            }
        }
        return serverWithLowestCount;
    }

    private boolean isOtherUnderMin(BackendServer other) {
        for (BackendServer backendServer : getAvailableServers()) {
            int playerCount = Nebula.server.getServer(backendServer.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount < Data.defaultmin && !backendServer.equals(other)) {
                return true;
            }
        }
        return false;
    }

    private BackendServer getServerBetweenMinAndMaxPlayers() {
        for (BackendServer backendServer : getAvailableServers()) {
            int playerCount = Nebula.server.getServer(backendServer.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount >= Data.defaultmin && playerCount < Data.defaultmax) {
                return backendServer;
            }
        }
        return null;
    }

    public void createDefault() {
        String name = "Lobby-" + Nebula.util.generateUniqueString();
        BackendServer temp = Nebula.serverManager.createFromTemplate(
                Data.defaultServerTemplate,
                name,
                Nebula.server.getConsoleCommandSource(),
                "lobby", "retry"
        );
    }
}