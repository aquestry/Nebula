package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.ExternalServerManager;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class DefaultManager {

    private final ProxyServer server;
    private final ExternalServerManager externalServerManager;
    private final DataHolder dataHolder;
    private final Logger logger;
    private final List<BackendServer> defaults = new ArrayList<>();

    public DefaultManager(DataHolder dataHolder, ProxyServer server, ExternalServerManager externalServerManager, Logger logger) {
        this.dataHolder = dataHolder;
        this.server = server;
        this.externalServerManager = externalServerManager;
        this.logger = logger;
    }

    public RegisteredServer getDefaultServer() {
        return server.getServer(Nebula.util.getRandomElement(defaults).getServerName()).get();
    }

    public void refresh() {
        String[] splitConfig = Data.newCreateCount.split("/");
        int newCreateThreshold = Integer.parseInt(splitConfig[0]);
        int maxPlayersPerServer = Integer.parseInt(splitConfig[1]);

        boolean onebelowCreateThreshold = false;
        defaults.clear();
        for (BackendServer backendServer : dataHolder.backendInfoMap) {
            int pCount = server.getServer(backendServer.getServerName()).get().getPlayersConnected().size();
            if (backendServer.isOnline() && backendServer.getTemplate().equals(Data.defaultServerTemplate) && pCount < maxPlayersPerServer) {
                defaults.add(backendServer);
            }
        }
        for(BackendServer backendServer : defaults) {
            int pCount = server.getServer(backendServer.getServerName()).get().getPlayersConnected().size();
            boolean foundOtherone = false;
            if(pCount >= newCreateThreshold) {
                for(BackendServer b : defaults) {
                    int OtherPlayerCount = server.getServer(b.getServerName()).get().getPlayersConnected().size();
                    if(!b.getServerName().equals(backendServer.getServerName()) && OtherPlayerCount > newCreateThreshold) {
                        foundOtherone = true;
                    }
                }
            }
            if(!foundOtherone) {
                externalServerManager.createFromTemplate(
                        Util.getRandomElement(dataHolder.holdServerMap),
                        Data.defaultServerTemplate,
                        "default-" + getDefaultsCount(),
                        server.getConsoleCommandSource());
            }
        }



    }
    public int getDefaultsCount() {
        List<BackendServer> defaults = new ArrayList<>();
        for(BackendServer backendServer : dataHolder.backendInfoMap) {
            if(backendServer.getTemplate().equals(Data.defaultServerTemplate))  {
                defaults.add(backendServer);
            }
        }
        return defaults.size();
    }
}
