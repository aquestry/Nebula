package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.BackendServer;
import de.voasis.nebula.Nebula;
import java.util.ArrayList;

public class ProxyShutdown {
    public ProxyShutdown(ProxyShutdownEvent event) {
        for(BackendServer backendServer : new ArrayList<>(Data.backendInfoMap)) {
            if(backendServer != null) {
                Nebula.serverManager.delete(backendServer, null);
            }
        }
        Nebula.ssh.closeAll();
    }
}