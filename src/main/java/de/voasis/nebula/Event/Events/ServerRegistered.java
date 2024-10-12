package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.proxy.server.ServerRegisteredEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.voasis.nebula.Helper.DataHolder;
import org.slf4j.Logger;

public class ServerRegistered {
    public ServerRegistered(ServerRegisteredEvent event, DataHolder dataHolder, Logger logger, ProxyServer server) {
        RegisteredServer reg = event.registeredServer();
        ServerInfo info = reg.getServerInfo();
        if (reg.getServerInfo().getName().equals("default")) {
            logger.info("Default-Server registered.");
            dataHolder.defaultRegisteredServer = server.registerServer(reg.getServerInfo());
        }
        logger.info("Server registered: {}, IP: {}", reg.getServerInfo().getName(), reg.getServerInfo().getAddress());
    }
}
