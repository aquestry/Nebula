package de.voasis.serverHandlerProxy;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.slf4j.Logger;
import java.util.*;


public class DataHolder {
    public String defaultServer = null;
    public RegisteredServer defaultRegisteredServer = null;
    public List<ServerInfo> serverInfoMap = new ArrayList<>();
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
            serverInfoMap.add(new ServerInfo(name, ip, port, password));
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

    public List<String> getServerNames() {
        List<String> serverNames = new ArrayList<>();
        for (ServerInfo serverInfo : serverInfoMap) {
            serverNames.add(serverInfo.getServerName());
        }
        return serverNames;
    }

    public List<ServerInfo> getAllInfos() {
        return new ArrayList<>(serverInfoMap);
    }
}
