package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Helper.DataHolder;
import org.slf4j.Logger;

public class ServerUnregistered {
    public ServerUnregistered(ServerUnregisteredEvent event, DataHolder dataHolder, Logger logger) {
        RegisteredServer unreg = event.unregisteredServer();
        dataHolder.holdServerMap.removeIf(serverInfo -> serverInfo.getServerName().equals(unreg.getServerInfo().getName()));
        logger.info("Server unregistered: {}, IP: {}", unreg.getServerInfo().getName(), unreg.getServerInfo().getAddress());
    }
}
