package de.voasis.nebula.event.events;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Container;
import de.voasis.nebula.Nebula;
import java.util.ArrayList;

public class ProxyShutdown {
    public ProxyShutdown(ProxyShutdownEvent event) {
        Config.quitting = true;
        for(Container container : new ArrayList<>(Config.containerMap)) {
            if(container != null) {
                Nebula.containerManager.delete(container, null);
            }
        }
        Nebula.ssh.closeAll();
    }
}