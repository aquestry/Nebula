package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.HoldServer;
import de.voasis.nebula.Nebula;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DataHolder {

    public List<HoldServer> holdServerMap = new ArrayList<>();
    public List<BackendServer> backendInfoMap = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger("nebula");
    private final YamlDocument config;
    private final ProxyServer server;

    public DataHolder(YamlDocument config, ProxyServer server) {
        this.config = config;
        this.server = server;
    }
    public void Refresh() {
        holdServerMap.clear();
        Data.adminUUIDs.clear();
        Data.defaultServerTemplate = config.getString("default-template");
        Data.defaultmax = config.getInt("default-max");
        Data.defaultmin = config.getInt("default-min");
        Data.vsecret = config.getString("vsecret");
        Data.adminUUIDs = List.of(config.getString("admins").split(","));
        logger.info("Default-Max: {}", Data.defaultmax);
        logger.info("Default-Min: {}", Data.defaultmin);
        logger.info("Loading servers from config...");
        Set<Object> managerServerKeys = config.getSection("manager-servers").getKeys();
        for (Object serverName : managerServerKeys) {
            String name = (String) serverName;
            String ip = config.getString("manager-servers." + name + ".ip");
            String password = config.getString("manager-servers." + name + ".password");
            String username = config.getString("manager-servers." + name + ".username");
            HoldServer holdServer = new HoldServer(name, ip, password, 0, username);
            holdServerMap.add(holdServer);
            Nebula.util.updateFreePort(holdServer);
            logger.info("Added hold server to pool: {}", name);
        }
    }

    public BackendServer getBackendServer(String name) {
        for (BackendServer server : backendInfoMap) {
            if (server.getServerName().equals(name)) {
                return server;
            }
        }
        return null;
    }
}
