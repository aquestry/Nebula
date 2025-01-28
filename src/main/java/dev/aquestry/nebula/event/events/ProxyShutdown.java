package dev.aquestry.nebula.event.events;

import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.model.Container;
import dev.aquestry.nebula.Nebula;
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