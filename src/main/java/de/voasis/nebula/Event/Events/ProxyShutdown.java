package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class ProxyShutdown {
    public ProxyShutdown(ProxyShutdownEvent event, Logger logger, ProxyServer server) {
        logger.info("Deleting Servers...");
        List<BackendServer> serversToDelete = new ArrayList<>(Nebula.dataHolder.backendInfoMap);
        for (BackendServer backendServer : serversToDelete) {
            Nebula.externalServerManager.delete(backendServer.getHoldServer(), backendServer.getServerName(), server.getConsoleCommandSource());
        }
    }
}
