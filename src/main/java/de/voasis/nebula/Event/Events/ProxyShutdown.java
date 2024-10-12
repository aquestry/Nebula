package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.ExternalServerManager;
import de.voasis.nebula.Helper.DataHolder;
import de.voasis.nebula.Maps.BackendServer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ProxyShutdown {
    public ProxyShutdown(ProxyShutdownEvent event, DataHolder dataHolder, ExternalServerManager externalServerManager, Logger logger, ProxyServer server) {
        logger.info("Deleting Servers...");
        List<BackendServer> serversToDelete = new ArrayList<>(dataHolder.backendInfoMap);
        for (BackendServer backendServer : serversToDelete) {
            externalServerManager.delete(backendServer.getHoldServer(), backendServer.getServerName(), server.getConsoleCommandSource());
        }
    }
}
