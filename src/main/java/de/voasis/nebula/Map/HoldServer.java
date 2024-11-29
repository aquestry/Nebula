package de.voasis.nebula.Map;

import de.voasis.nebula.Data.Data;
import java.util.List;

public class HoldServer {

    private final String serverName;
    private final String password;
    private final String username;
    private final String ip;
    private int freePort;

    public HoldServer(String serverName, String ip, String password, int freePort, String username) {
        this.serverName = serverName;
        this.password = password;
        this.username = username;
        this.ip = ip;
        this.freePort = freePort;
    }

    public String getServerName() { return serverName; }
    public String getPassword() { return password; }
    public String getUsername() { return username; }
    public String getIp() { return ip; }
    public int getFreePort() { return freePort; }
    public void setFreePort(int freePort) { this.freePort = freePort; }

    public List<BackendServer> getBackendServers() {
        return Data.backendInfoMap.stream().filter(backendServer -> backendServer.getHoldServer().equals(this)).toList();
    }
}
