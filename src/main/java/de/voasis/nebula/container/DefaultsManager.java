package de.voasis.nebula.container;

import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Container;
import de.voasis.nebula.Nebula;
import java.util.ArrayList;
import java.util.List;

public class DefaultsManager {
    public Container getTarget() {
        Container target = getServerWithLowestPlayerCount();
        if(target != null) {
            int count = Nebula.server.getServer(target.getServerName()).get().getPlayersConnected().size();
            if(count + 1 == Config.defaultmin && !isOtherUnderMin(target)) {
                createDefault();
            }
        }
        target = getServerBetweenMinAndMaxPlayers();
        if(target != null) {
            return target;
        }
        return getServerWithLowestPlayerCount();
    }

    private List<Container> getAvailableServers() {
        List<Container> servers = new ArrayList<>();
        for (Container server : Config.backendInfoMap) {
            if (server.getFlags().contains("lobby") && server.isOnline()) {
                servers.add(server);
            }
        }
        return servers;
    }

    private Container getServerWithLowestPlayerCount() {
        if (getAvailableServers().isEmpty()) {
            return null;
        }
        Container serverWithLowestCount = getAvailableServers().getFirst();
        int lowestPlayerCount = Nebula.server.getServer(serverWithLowestCount.getServerName())
                .get()
                .getPlayersConnected()
                .size();
        for (Container container : getAvailableServers()) {
            int playerCount = Nebula.server.getServer(container.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount < lowestPlayerCount) {
                serverWithLowestCount = container;
                lowestPlayerCount = playerCount;
            }
        }
        return serverWithLowestCount;
    }

    private boolean isOtherUnderMin(Container other) {
        for (Container container : getAvailableServers()) {
            int playerCount = Nebula.server.getServer(container.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount < Config.defaultmin && !container.equals(other)) {
                return true;
            }
        }
        return false;
    }

    private Container getServerBetweenMinAndMaxPlayers() {
        for (Container container : getAvailableServers()) {
            int playerCount = Nebula.server.getServer(container.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount >= Config.defaultmin && playerCount < Config.defaultmax) {
                return container;
            }
        }
        return null;
    }

    public void createDefault() {
        String name = "Lobby-" + Nebula.util.generateUniqueString();
        Container temp = Nebula.containerManager.createFromTemplate(
                Config.defaultServerTemplate,
                name,
                Nebula.server.getConsoleCommandSource(),
                "lobby", "retry"
        );
    }
}