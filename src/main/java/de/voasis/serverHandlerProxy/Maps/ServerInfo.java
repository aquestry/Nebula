package de.voasis.serverHandlerProxy.Maps;

public class ServerInfo {

    private String serverName;
    private String ip;
    private String password;
    private int freePort;
    private String username;

    public ServerInfo(String serverName, String ip, String password, int freePort, String username) {
        this.serverName = serverName;
        this.password = password;
        this.freePort = freePort;
        this.username = username;
        this.ip = ip;
    }
    public String getIp() {
        return ip;
    }
    public String getServerName() {
        return serverName;
    }
    public String getPassword() {
        return password;
    }
    public int getFreePort() {
        return freePort;
    }
    public String getUsername() {
        return username;
    }
    public void setFreePort(int freePort) {
        this.freePort = freePort;
    }

}
