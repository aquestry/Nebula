package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.proxy.server.ServerUnregisteredEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

public class ServerUnregistered {
    public ServerUnregistered(ServerUnregisteredEvent event, Logger logger) {
        RegisteredServer unreg = event.unregisteredServer();
        logger.info("Server unregistered: {}, IP: {}", unreg.getServerInfo().getName(), unreg.getServerInfo().getAddress());
    }
}
