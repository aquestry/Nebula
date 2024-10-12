package de.voasis.nebula.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Maps.GamemodeInfo;
import de.voasis.nebula.Maps.QueueInfo;
import de.voasis.nebula.Maps.HoldServer;
import de.voasis.nebula.Nebula;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.slf4j.Logger;
import java.util.*;


public class DataHolder {
    public String defaultServerTemplate = null;
    public RegisteredServer defaultRegisteredServer = null;
    public List<String> admins = new ArrayList<>();

    public List<HoldServer> holdServerMap = new ArrayList<>();
    public List<BackendServer> backendInfoMap = new ArrayList<>();
    public List<GamemodeInfo> gamemodeInfoMap = new ArrayList<>();
    public List<QueueInfo> queues = new ArrayList<>();

    public void Refresh(YamlDocument config, ProxyServer server, Logger logger) {
        defaultServerTemplate = config.getString("default-template");
        Messages.vsecret = config.getString("vsecret");
        holdServerMap.clear();
        admins.clear();
        admins = List.of(config.getString("admins").split(","));

        logger.info("Loading servers from config...");
        Set<Object> managerServerKeys = config.getSection("manager-servers").getKeys();
        for (Object serverName : managerServerKeys) {
            String name = (String) serverName;
            String ip = config.getString("manager-servers." + name + ".ip");
            String password = config.getString("manager-servers." + name + ".password");
            String username = config.getString("manager-servers." + name + ".username");
            HoldServer holdServer = new HoldServer(name, ip, password, 0, username);
            holdServerMap.add(holdServer);
            logger.info("Added Server to pool: {}", name);
            Nebula.util.updateFreePort(holdServer);
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
