package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.GamemodeInfo;
import de.voasis.nebula.Maps.QueueInfo;
import de.voasis.nebula.Maps.HoldServer;
import de.voasis.nebula.Nebula;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DataHolder {

    public List<HoldServer> holdServerMap = new ArrayList<>();
    public List<BackendServer> backendInfoMap = new ArrayList<>();
    public List<GamemodeInfo> gamemodeInfoMap = new ArrayList<>();
    public List<QueueInfo> queues = new ArrayList<>();

    private final YamlDocument config;
    private static final Logger logger = LoggerFactory.getLogger("nebula");
    private final ProxyServer server;


    public DataHolder(YamlDocument config, ProxyServer server) {
        this.config = config;
        this.server = server;
    }
    public void Refresh() {

        holdServerMap.clear();
        Data.adminUUIDs.clear();
        Data.defaultServerTemplate = config.getString("default-template");
        Data.newCreateCount = config.getString("default-new-create-count");
        Data.vsecret = config.getString("vsecret");
        Data.adminUUIDs = List.of(config.getString("admins").split(","));
        List<String> topull = new ArrayList<>();
        topull.add(Data.defaultServerTemplate);

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
            logger.info("Added Server to pool: {}", name);
        }

        logger.info("Loading Gamemodes from config...");
        Set<Object> gamemodesKeys = config.getSection("gamemodes").getKeys();
        for (Object gamemode : gamemodesKeys) {
            String name = (String) gamemode;
            String templateName = config.getString("gamemodes." + name + ".templateName");
            int neededPlayers = config.getInt("gamemodes." + name + ".neededPlayers");
            logger.info("Registered Gamemode: {}", name);
            GamemodeInfo newGamemode = new GamemodeInfo(name, neededPlayers, templateName);
            gamemodeInfoMap.add(newGamemode);
            queues.add(new QueueInfo(newGamemode));
            topull.add(templateName);
        }
        // Pull Templates
        for (HoldServer holdServer : holdServerMap) {
            for(String templateName : topull) {
                Nebula.serverManager.pull(holdServer, templateName);
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
    public RegisteredServer getServer(String name) {
        for (RegisteredServer server : server.getAllServers()) {
            if (server.getServerInfo().getName().equals(name)) {
                return server;
            }
        }
        return null;
    }
}
