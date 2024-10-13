package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

public class ServerRegistered {
    public ServerRegistered(ServerRegisteredEvent event, Logger logger, ProxyServer server) {
        RegisteredServer reg = event.registeredServer();
        logger.info("Server registered: {}, IP: {}", reg.getServerInfo().getName(), reg.getServerInfo().getAddress());
    }
}
