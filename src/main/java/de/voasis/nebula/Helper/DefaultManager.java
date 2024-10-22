package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.ServerManager;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class DefaultManager {
    private final ProxyServer server;
    private final ServerManager serverManager;
    private final DataHolder dataHolder;
    private static final Logger logger = LoggerFactory.getLogger("nebula");
    private final List<BackendServer> defaults = new ArrayList<>();
    private final List<BackendServer> available = new ArrayList<>();
    private final int min;
    private final int max;

    public DefaultManager(ProxyServer server) {
        this.dataHolder = Nebula.dataHolder;
        this.serverManager = Nebula.serverManager;
        this.server = server;
        String[] splitConfig = Data.newCreateCount.split("/");
        min = Integer.parseInt(splitConfig[0]);
        max = Integer.parseInt(splitConfig[1]);
    }

    public RegisteredServer getDefault() {
        for(BackendServer backendServer : dataHolder.backendInfoMap) {
            if(backendServer.getTag().equals("default") && !defaults.contains(backendServer)) {
                defaults.add(backendServer);
            }
        }
        for(BackendServer backendServer : defaults) {
            if(backendServer.isOnline() && !available.contains(backendServer)) {
                available.add(backendServer);
            }
        }
        BackendServer between = getServerBetweenMinAndMaxPlayers();
        if(between != null) {
            createNewDefaultServer();
            logger.info("Sending to: {}", between.getServerName());
            return dataHolder.getServer(between.getServerName());
        }
        BackendServer under = getServerUnderMin();
        if(under != null) {
            logger.info("Sending to: {}", under.getServerName());
            return dataHolder.getServer(under.getServerName());
        }
        BackendServer target = getServerWithLowestPlayerCount();
        createNewDefaultServer();
        logger.info("Sending to: {}", target.getServerName());
        return dataHolder.getServer(target.getServerName());
    }
    private BackendServer getServerWithLowestPlayerCount() {
        if (available.isEmpty()) {
            return null;
        }
        BackendServer serverWithLowestCount = available.getFirst();
        int lowestPlayerCount = server.getServer(serverWithLowestCount.getServerName())
                .get()
                .getPlayersConnected()
                .size();
        for (BackendServer backendServer : available) {
            int playerCount = server.getServer(backendServer.getServerName())
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
    private BackendServer getServerBetweenMinAndMaxPlayers() {
        for (BackendServer backendServer : available) {
            int playerCount = server.getServer(backendServer.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount > min && playerCount < max) {
                return backendServer;
            }
        }
        return null;
    }
    private BackendServer getServerUnderMin() {
        for (BackendServer backendServer : available) {
            int playerCount = server.getServer(backendServer.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount < min) {
                return backendServer;
            }
        }
        return null;
    }

    public BackendServer createNewDefaultServer() {
        String name = "default-" + defaults.size();
        serverManager.createFromTemplate(
                Util.getRandomElement(dataHolder.holdServerMap),
                Data.defaultServerTemplate,
                name,
                server.getConsoleCommandSource(),
                "default"
        );
        return dataHolder.getBackendServer("default-" + defaults.size());
    }
    public int getMax() { return max; }
}