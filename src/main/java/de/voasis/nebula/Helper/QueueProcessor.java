package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.QueueInfo;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class QueueProcessor {
    static ProxyServer server;
    private final Logger logger = LoggerFactory.getLogger("nebula");

    public QueueProcessor(ProxyServer server) {
        QueueProcessor.server = server;
    }

    public void process() {
        for (QueueInfo queue : Nebula.dataHolder.queues) {
            if (queue.getPlayerCount() >= queue.getGamemode().getNeededPlayers()) {
                if(queue.getUsed()) {return;}
                queue.setUsed(true);
                logger.info("Enough players, creating server...");
                String newName = queue.getGamemode().getName() + "-" + (Nebula.dataHolder.backendInfoMap.size() + 1);
                Nebula.serverManager.createFromTemplate(
                        Util.getRandomElement(Nebula.dataHolder.holdServerMap),
                        queue.getGamemode().getTemplateName(),
                        newName,
                        server.getConsoleCommandSource(),
                        "queue"
                );
                BackendServer backendServer = Nebula.dataHolder.getBackendServer(newName);
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
