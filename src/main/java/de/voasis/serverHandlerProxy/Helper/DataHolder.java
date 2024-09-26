package de.voasis.serverHandlerProxy.Helper;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import de.voasis.serverHandlerProxy.ServerHandlerProxy;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.slf4j.Logger;
import java.util.*;


public class DataHolder {
    public String defaultServer = null;
    public RegisteredServer defaultRegisteredServer = null;
    public List<ServerInfo> serverInfoMap = new ArrayList<>();
    public List<BackendServer> backendInfoMap = new ArrayList<>();
    public List<String> admins = new ArrayList<>();

    public void Refresh(YamlDocument config, ProxyServer server, Logger logger) {
        defaultServer = config.getString("default-server");
        serverInfoMap.clear();
        admins.clear();
        admins = List.of(config.getString("admins").split(","));
        Set<Object> managerServerKeys = config.getSection("manager-servers").getKeys();
        for (Object serverName : managerServerKeys) {
            String name = (String) serverName;
            String ip = config.getString("manager-servers." + name + ".ip");
            String port = config.getString("manager-servers." + name + ".port");
            String password = config.getString("manager-servers." + name + ".password");
            ServerInfo serverInfo = new ServerInfo(name, ip, port, password, null);
            ServerHandlerProxy.pingUtil.updateFreePort(serverInfo);
            serverInfoMap.add(serverInfo);
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
