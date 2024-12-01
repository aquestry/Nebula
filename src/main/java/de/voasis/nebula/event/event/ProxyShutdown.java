package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.BackendServer;
import de.voasis.nebula.Nebula;
import java.util.ArrayList;
import java.util.List;

public class ProxyShutdown {
    public ProxyShutdown(ProxyShutdownEvent event) {
        List<BackendServer> serversToDelete = new ArrayList<>(Data.backendInfoMap);
        for(BackendServer backendServer : serversToDelete) {
            if(backendServer != null) {
                Nebula.serverManager.delete(backendServer, null);
            }
        }
    }
}