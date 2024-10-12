package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.ExternalServerManager;
import de.voasis.nebula.Helper.DataHolder;
import org.slf4j.Logger;

public class ProxyShutdown {
    public ProxyShutdown(ProxyShutdownEvent event, DataHolder dataHolder, ExternalServerManager externalServerManager, Logger logger, ProxyServer server) {
        logger.info("Deleting Default-Server...");
        externalServerManager.delete(dataHolder.holdServerMap.getFirst(), "default", server.getConsoleCommandSource());
    }
}
