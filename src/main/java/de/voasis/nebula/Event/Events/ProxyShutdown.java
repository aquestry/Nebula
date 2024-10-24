package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

public class ProxyShutdown {
    private final Logger logger = LoggerFactory.getLogger("nebula");
    public ProxyShutdown(ProxyShutdownEvent event, ProxyServer server) {
        List<BackendServer> serversToDelete = new ArrayList<>(Nebula.dataHolder.backendInfoMap);
        for(BackendServer backendServer : serversToDelete) {
            if(backendServer != null) {
                Nebula.serverManager.delete(backendServer, null);
            }
        }
    }
}
