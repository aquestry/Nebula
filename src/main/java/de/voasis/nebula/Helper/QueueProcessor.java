package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.QueueInfo;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import java.util.List;

public class QueueProcessor {
    static DataHolder dataHolder;
    static ProxyServer server;
    static Logger logger;

    public QueueProcessor(ProxyServer server, Logger logger) {
        QueueProcessor.server = server;
        QueueProcessor.dataHolder = Nebula.dataHolder;
        QueueProcessor.logger = logger;
    }

    public void process() {
        for (QueueInfo queue : dataHolder.queues) {
            if (queue.getPlayerCount() >= queue.getGamemode().getNeededPlayers()) {
                if(queue.getUsed()) {return;}
                queue.setUsed(true);
                logger.info("Enough players, creating server...");
                String newName = queue.getGamemode().getName() + "-" + (dataHolder.backendInfoMap.size() + 1);
                Nebula.serverManager.createFromTemplate(
                        Util.getRandomElement(dataHolder.holdServerMap),
                        queue.getGamemode().getTemplateName(),
                        newName,
                        server.getConsoleCommandSource(),
                        "queue"
                );
                BackendServer backendServer = dataHolder.getBackendServer(newName);
                if (backendServer != null) {
                    int playersToAdd = queue.getGamemode().getNeededPlayers();
                    List<Player> players = queue.getPlayers().subList(0, playersToAdd);
                    for (Player player : players) {
                        backendServer.addPendingPlayerConnection(player);
                        queue.removePlayer(player);
                    }
                }

            }
        }
    }
}
