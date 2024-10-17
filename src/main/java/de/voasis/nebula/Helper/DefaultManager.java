package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.ExternalServerManager;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class DefaultManager {
    private final ProxyServer server;
    private final ExternalServerManager externalServerManager;
    private final DataHolder dataHolder;
    private final Logger logger;
    private final List<BackendServer> defaults = new ArrayList<>();
    private final List<BackendServer> available = new ArrayList<>();
    private final int min;
    private final int max;

    public DefaultManager(ProxyServer server,  Logger logger) {
        this.dataHolder = Nebula.dataHolder;
        this.externalServerManager = Nebula.serverManager;
        this.server = server;
        this.logger = logger;
        String[] splitConfig = Data.newCreateCount.split("/");
        min = Integer.parseInt(splitConfig[0]);
        max = Integer.parseInt(splitConfig[1]);
    }

    public RegisteredServer getDefault() {
        logger.info("Get default server Method");
        for(BackendServer backendServer : dataHolder.backendInfoMap) {
            if(backendServer.getTag().equals("default") && !defaults.contains(backendServer)) {
                defaults.add(backendServer);
                logger.info("Added backend server default list: {}", backendServer.getServerName());
            }
        }
        for(BackendServer backendServer : defaults) {
            if(backendServer.isOnline() && !available.contains(backendServer)) {
                available.add(backendServer);
                logger.info("Added backend server available list: {}", backendServer.getServerName());
            }
        }
        BackendServer between = getServerBetweenMinAndMaxPlayers();
        if(between != null) {
            createNewDefaultServer();
            logger.info("Creating new server and returning: ", between.getServerName());
            return server.getServer(between.getServerName()).get();
        }
        BackendServer under = getServerUnderMin();
        if(under != null) {
            logger.info("Returning: ", under.getServerName());
            return server.getServer(under.getServerName()).get();
        }
        logger.info("Creating new server and returning: ", getServerWithLowestPlayerCount().getServerName());
        createNewDefaultServer();
        return server.getServer(getServerWithLowestPlayerCount().getServerName()).get();
    }
    private BackendServer getServerWithLowestPlayerCount() {
        if (available.isEmpty()) {
            logger.info("Available list is empty, trying default-0");
            return dataHolder.getBackendServer("default-0");
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
            if (playerCount >= min && playerCount < max) {
                return backendServer;
            }
        }
        return dataHolder.getBackendServer("default-0");
    }
    private BackendServer getServerUnderMin() {
        logger.info("getServerUnderMin Method");
        for (BackendServer backendServer : available) {
            int playerCount = server.getServer(backendServer.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount <= min) {
                logger.info("returning: ", backendServer.getServerName());
                return backendServer;
            }
        }
        return dataHolder.getBackendServer("default-0");
    }

    public BackendServer createNewDefaultServer() {
        String name = "default-" + defaults.size();
        externalServerManager.createFromTemplate(
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