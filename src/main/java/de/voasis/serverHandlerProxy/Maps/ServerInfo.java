package de.voasis.serverHandlerProxy.Maps;

public class ServerInfo {
    private String serverName;
    private String ip;
    private int port;
    private String password;
    private int freePort;

    public ServerInfo(String name, String ip, int port, String password, int freePort) {
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
    public int getPort() {
        return port;
    }
    public String getPassword() {
        return password;
    }
    public String getFreePort() {
        return password;
    }
    public void setFreePort(int freePort) {
        this.freePort = freePort;
    }
}