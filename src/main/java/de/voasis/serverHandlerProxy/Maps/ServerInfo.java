package de.voasis.serverHandlerProxy.Maps;

public class ServerInfo {
    private String serverName;
    private String ip;
    private String port;
    private String password;
    private String freePort;

    public ServerInfo(String name, String ip, String port, String password, String freePort) {
        this.serverName = name;
        this.ip = ip;
        this.port = port;
        this.password = password;
        this.freePort = freePort;
    }
    public String getServerName() {
        return serverName;
    }
    public String getIp() {
        return ip;
    }
    public String getPort() {
        return port;
    }
    public String getPassword() {
        return password;
    }
    public String getFreePort() {
        return password;
    }
    public void setFreePort(String freePort) {
        this.freePort = freePort;
    }
}