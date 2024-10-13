package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.ExternalServerManager;
import de.voasis.nebula.Maps.BackendServer;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DefaultManager {
    private final ProxyServer server;
    private final ExternalServerManager externalServerManager;
    private final DataHolder dataHolder;
    private final Logger logger;
    private final List<BackendServer> defaults = new ArrayList<>();
    private final List<BackendServer> available = new ArrayList<>();

    public DefaultManager(DataHolder dataHolder, ProxyServer server, ExternalServerManager externalServerManager, Logger logger) {
        this.dataHolder = dataHolder;
        this.server = server;
        this.externalServerManager = externalServerManager;
        this.logger = logger;
    }

    public void refresh() {
        String[] splitConfig = Data.newCreateCount.split("/");
        int min = Integer.parseInt(splitConfig[0]);
        int max = Integer.parseInt(splitConfig[1]);

        for (BackendServer backendServer : dataHolder.backendInfoMap) {
            if (backendServer.getTag().equals("default") && !defaults.contains(backendServer)) {
                defaults.add(backendServer);
                logger.info("Added {} to default servers list.", backendServer.getServerName());
            }
        }

        boolean serverUnder = false;
        for (BackendServer backendServer : defaults) {
            int playerCount = server.getServer(backendServer.getServerName()).get().getPlayersConnected().size();
            if(playerCount < max && backendServer.isOnline() && !available.contains(backendServer)) {
                available.add(backendServer);
                logger.info("Added {} to available servers list.", backendServer.getServerName());
            }
            if(playerCount < min) {
                serverUnder = true;
            }
        }
        if(!serverUnder) {
            createNewDefaultServer();
            logger.info("Creating new default server");
        }

        available.removeIf(backendServer -> !dataHolder.backendInfoMap.contains(backendServer));
        defaults.removeIf(backendServer -> !dataHolder.backendInfoMap.contains(backendServer));
    }

    public void createNewDefaultServer() {
        externalServerManager.createFromTemplate(
                Util.getRandomElement(dataHolder.holdServerMap),
                Data.defaultServerTemplate,
                "default-" + defaults.size(),
                server.getConsoleCommandSource(),
                "default"
        );
    }


    public RegisteredServer getDefaultServer() {
        Optional<RegisteredServer> lowestPopulationServer = available.stream()
                .map(backendServer -> server.getServer(backendServer.getServerName()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.comparingInt(s -> s.getPlayersConnected().size()));
        return lowestPopulationServer.orElse(null);
    }

    public void connectPlayerToDefaultServer(Player player) {
        RegisteredServer targetServer = getDefaultServer();
        if (targetServer != null) {
            player.createConnectionRequest(targetServer).fireAndForget();
            logger.info("Connecting player {} to server {}", player.getUsername(), targetServer.getServerInfo().getName());
        } else {
            logger.error("No default server available to connect player {}", player.getUsername());
        }
    }
}