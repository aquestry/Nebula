package de.voasis.serverHandlerProxy.Maps;

public class ServerInfo {
    private String serverName;
    private String ip;
    private String port;
    private String password;

    public ServerInfo(String name, String ip, String port, String password) {
        this.serverName = name;
        this.ip = ip;
        this.port = port;
        this.password = password;
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
}