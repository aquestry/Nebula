package de.voasis.serverHandlerProxy.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Maps.GamemodeInfo;
import de.voasis.serverHandlerProxy.Maps.QueueInfo;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import de.voasis.serverHandlerProxy.ServerHandlerProxy;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class DataHolder {
    static final Logger logger = LoggerFactory.getLogger("serverhandlerproxy");
    public String defaultServerTemplate = null;
    public RegisteredServer defaultRegisteredServer = null;
    public List<String> admins = new ArrayList<>();

    public List<ServerInfo> serverInfoMap = new ArrayList<>();
    public List<BackendServer> backendInfoMap = new ArrayList<>();
    public List<GamemodeInfo> gamemodeInfoMap = new ArrayList<>();
    public List<QueueInfo> queues = new ArrayList<>();

    public void Refresh(YamlDocument config, ProxyServer server, Logger logger) {
        defaultServerTemplate = config.getString("default-template");
        Messages.vsecret = config.getString("vsecret");
        serverInfoMap.clear();
        admins.clear();
        admins = List.of(config.getString("admins").split(","));

        logger.info("Loading servers from config...");
        Set<Object> managerServerKeys = config.getSection("manager-servers").getKeys();
        for (Object serverName : managerServerKeys) {
            String name = (String) serverName;
            String ip = config.getString("manager-servers." + name + ".ip");
            String password = config.getString("manager-servers." + name + ".password");
            String username = config.getString("manager-servers." + name + ".username");
            ServerInfo serverInfo = new ServerInfo(name, ip, password, 0, username);
            serverInfoMap.add(serverInfo);
            logger.info("Added Server to pool: " + name);
            ServerHandlerProxy.pingUtil.updateFreePort(serverInfo);
        }

        logger.info("Loading Gamemodes from config...");
        Set<Object> gamemodesKeys = config.getSection("gamemodes").getKeys();
        for (Object gamemode : gamemodesKeys) {
            String name = (String) gamemode;
            String templateName = config.getString("gamemodes." + name + ".templateName");
            int neededPlayers = config.getInt("gamemodes." + name + ".neededPlayers");
            logger.info("Registered Gamemode: " + name);
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
