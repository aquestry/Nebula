package de.voasis.serverHandlerProxy.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Maps.Messages;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import de.voasis.serverHandlerProxy.ServerHandlerProxy;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class DataHolder {
    private static final Logger log = LoggerFactory.getLogger(DataHolder.class);
    public String defaultServer = null;
    public RegisteredServer defaultRegisteredServer = null;
    public List<ServerInfo> serverInfoMap = new ArrayList<>();
    public List<BackendServer> backendInfoMap = new ArrayList<>();
    public List<String> admins = new ArrayList<>();

    public void Refresh(YamlDocument config, ProxyServer server, Logger logger) {
        defaultServer = config.getString("default-server");
        Messages.vsecret = config.getString("vsecret");
        serverInfoMap.clear();
        admins.clear();
        admins = List.of(config.getString("admins").split(","));
        Set<Object> managerServerKeys = config.getSection("manager-servers").getKeys();

        logger.info("Loading servers from config...");

        for (Object serverName : managerServerKeys) {
            String name = (String) serverName;
            String ip = config.getString("manager-servers." + name + ".ip");
            String password = config.getString("manager-servers." + name + ".password");
            String username = config.getString("manager-servers." + name + ".username");
            ServerInfo serverInfo = new ServerInfo(name, ip, password, 0, username);
            serverInfoMap.add(serverInfo);
            log.info("Added Server to pool: " + name);
            ServerHandlerProxy.pingUtil.updateFreePort(serverInfo);
        }
    }

    public ServerInfo getServerInfo(String name) {
        for (ServerInfo serverInfo : serverInfoMap) {
            if (serverInfo.getServerName().equals(name)) {
                return serverInfo;
            }
        }
        return null;
    }
    public BackendServer getBackendServer(String name) {
        for (BackendServer server : backendInfoMap) {
            if (server.getServerName().equals(name)) {
                return server;
            }
        }
        return null;
    }
    public boolean getState(String backendServer) {
        for (BackendServer server : backendInfoMap) {
            if (server.getServerName().equals(backendServer)) {
                return server.getState();
            }
        }
        return false;
    }
}
