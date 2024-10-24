package de.voasis.nebula.Data;

import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.GamemodeQueue;
import de.voasis.nebula.Maps.HoldServer;
import de.voasis.nebula.Nebula;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class DataHolder {

    public List<HoldServer> holdServerMap = new ArrayList<>();
    public List<BackendServer> backendInfoMap = new ArrayList<>();
    public List<GamemodeQueue> gamemodeQueueMap = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger("nebula");
    private final YamlDocument config;
    private final ProxyServer server;

    public DataHolder(YamlDocument config, ProxyServer server) {
        this.config = config;
        this.server = server;
    }
    public void Load() {
        Data.defaultServerTemplate = config.getString("default-template");
        Data.defaultmax = config.getInt("default-max");
        Data.defaultmin = config.getInt("default-min");
        Data.vsecret = config.getString("vsecret");
        Data.adminUUIDs = List.of(config.getString("admins").split(","));
        logger.info("Admin UUIDS: " + Data.adminUUIDs);
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
        Set<Object> gamemodes = config.getSection("gamemodes").getKeys();
        for (Object queue : gamemodes) {
            String name = (String) queue;
            String template = config.getString("gamemodes." + name + ".templateName");
            int needed = config.getInt("gamemodes." + name + ".neededPlayers");
            gamemodeQueueMap.add(new GamemodeQueue(name, template, needed));
            logger.info("Added gamemode to pool: {}, {}, {}.", name, template, needed);
        }

        for(HoldServer holdServer : holdServerMap){
            Nebula.serverManager.pull(holdServer, Data.defaultServerTemplate, server.getConsoleCommandSource());
            for(GamemodeQueue gamemode : gamemodeQueueMap) {
                Nebula.serverManager.pull(holdServer, gamemode.getTemplate(), server.getConsoleCommandSource());
            }
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
