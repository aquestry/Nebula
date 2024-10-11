package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.QueueInfo;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class QueueProcessor {
    static DataHolder dataHolder;
    static ProxyServer server;
    static final Logger logger = LoggerFactory.getLogger("nebula");

    public QueueProcessor(ProxyServer server, DataHolder dataHolder) {
        QueueProcessor.server = server;
        QueueProcessor.dataHolder = dataHolder;
    }

    public void process() {
        for (QueueInfo queue : dataHolder.queues) {
            if (queue.getPlayerCount() >= queue.getGamemode().getNeededPlayers()) {
                if(queue.getUsed()) {return;}
                queue.setUsed(true);
                logger.info("Enough players, creating server...");
                String newName = queue.getGamemode().getName() + "-" + (dataHolder.backendInfoMap.size() + 1);
                Nebula.externalServerManager.createFromTemplate(
                        getRandomElement(dataHolder.serverInfoMap),
                        queue.getGamemode().getTemplateName(),
                        newName,
                        null
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

    public <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Random r = new Random();
        int i = r.nextInt(list.size());
        return list.get(i);
    }
}
