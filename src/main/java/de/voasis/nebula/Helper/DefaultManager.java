package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.ExternalServerManager;
import de.voasis.nebula.Maps.BackendServer;
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


    public DefaultManager(DataHolder dataHolder, ProxyServer server, ExternalServerManager externalServerManager, Logger logger) {
        this.dataHolder = dataHolder;
        this.server = server;
        this.externalServerManager = externalServerManager;
        this.logger = logger;
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
            return server.getServer(getServerBetweenMinAndMaxPlayers().getServerName()).get();
        }
        BackendServer under = getServerUnderMin();
        if(under != null) {
            return server.getServer(getServerUnderMin().getServerName()).get();
        }
        createNewDefaultServer();
        return server.getServer(getServerWithLowestPlayerCount().getServerName()).get();
    }
    private BackendServer getServerWithLowestPlayerCount() {
        if (available.isEmpty()) {
            return null;
        }
        BackendServer serverWithLowestCount = available.get(0);
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
        return null;
    }
    private BackendServer getServerUnderMin() {
        for (BackendServer backendServer : available) {
            int playerCount = server.getServer(backendServer.getServerName())
                    .get()
                    .getPlayersConnected()
                    .size();
            if (playerCount <= min) {
                return backendServer;
            }
        }
        return null;
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