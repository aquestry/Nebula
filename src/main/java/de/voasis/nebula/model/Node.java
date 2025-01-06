package de.voasis.nebula.model;

import de.voasis.nebula.data.Config;
import java.util.List;

public class Node {

    private final String serverName;
    private final String password;
    private final String privateKeyFile;
    private final String username;
    private final String ip;
    private final int port;
    private int freePort;

    public Node(String serverName, String ip, String username, String password, String privateKeyFile, int port, int freePort) {
        this.serverName = serverName;
        this.password = (password != null) ? password : "none";
        this.privateKeyFile = (privateKeyFile != null) ? privateKeyFile : "none";
        this.username = username;
        this.ip = ip;
        this.port = port;
        this.freePort = freePort;
    }

    public void setFreePort(int freePort) { this.freePort = freePort; }
    public String getServerName() { return serverName; }
    public String getPassword() { return password; }
    public String getPrivateKeyFile() { return privateKeyFile; }
    public String getUsername() { return username; }
    public String getIp() { return ip; }
    public int getPort() { return port; }
    public int getFreePort() { return freePort; }

    public List<Container> getBackendServers() {
        return Config.containerMap.stream()
                .filter(backendServer -> backendServer.getHoldServer().equals(this))
                .toList();
    }
}