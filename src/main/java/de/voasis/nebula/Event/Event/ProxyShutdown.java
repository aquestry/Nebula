package de.voasis.nebula.Event.Event;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Map.BackendServer;
import de.voasis.nebula.Nebula;
import java.util.ArrayList;
import java.util.List;

public class ProxyShutdown {
    public ProxyShutdown(ProxyShutdownEvent event, ProxyServer server) {
        List<BackendServer> serversToDelete = new ArrayList<>(Data.backendInfoMap);
        for(BackendServer backendServer : serversToDelete) {
            if(backendServer != null) {
                Nebula.serverManager.delete(backendServer, null);
            }
        }
    }
}
