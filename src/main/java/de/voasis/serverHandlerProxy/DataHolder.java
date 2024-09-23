package de.voasis.serverHandlerProxy;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.slf4j.Logger;

import java.util.*;


public class DataHolder {
    public Optional<RegisteredServer> defaultServer;
    public List<ServerInfo> serverInfoMap;

    public void Refresh(YamlDocument config, ProxyServer server, Logger logger) {
        Optional<RegisteredServer> DS = server.getServer(config.getString("default-server"));


        if(DS.isPresent()) {
            defaultServer = DS;
        }
        serverInfoMap.clear();
        Set<Object> managerServerKeys = config.getSection("manager-servers").getKeys();
        for (Object serverName : managerServerKeys) {
            String ip = config.getString("manager-servers." + serverName + ".ip");
            int port = config.getInt("manager-servers." + serverName + ".port");
            String password = config.getString("manager-servers." + serverName + ".password");
            serverInfoMap.add(new ServerInfo(ip, port, password));
        }
    }
    public Optional<ServerInfo> getServerInfo(String name) {
        for(ServerInfo serverInfo : serverInfoMap) {
            if(serverInfo.getServerName().equals(name)) {
                return Optional.of(serverInfo);
            }
        }
        return Optional.empty();
    }
    public List<String> getServerNames() {
        List<String> serverNames = new ArrayList<>();
        for(ServerInfo serverInfo : serverInfoMap) {
            serverNames.add(serverInfo.getServerName());
        }
        return serverNames;
    }
}
