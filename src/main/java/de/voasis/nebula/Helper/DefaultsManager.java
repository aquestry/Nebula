package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultsManager {
    private final ProxyServer server;
    private final Logger logger = LoggerFactory.getLogger("nebula");

    public DefaultsManager(ProxyServer server) {
        this.server = server;
    }
    public BackendServer createDefault() {
        String name = "default-" + Nebula.dataHolder.backendInfoMap.stream().filter(backendServer -> backendServer.getTag().equals("default")).toList().size();
        Nebula.serverManager.createFromTemplate(
                Nebula.dataHolder.holdServerMap.getFirst(),
                Data.defaultServerTemplate,
                name,
                server.getConsoleCommandSource(),
                "default"
                );

        return Nebula.dataHolder.getBackendServer(name);
    }
}
