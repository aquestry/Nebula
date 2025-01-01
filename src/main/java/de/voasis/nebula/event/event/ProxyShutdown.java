package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Container;
import de.voasis.nebula.Nebula;
import java.util.ArrayList;

public class ProxyShutdown {
    public ProxyShutdown(ProxyShutdownEvent event) {
        Data.quitting = true;
        for(Container container : new ArrayList<>(Data.backendInfoMap)) {
            if(container != null) {
                Nebula.serverManager.delete(container, null);
            }
        }
        Nebula.ssh.closeAll();
    }
}