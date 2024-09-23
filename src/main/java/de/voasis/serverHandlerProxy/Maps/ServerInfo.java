package de.voasis.serverHandlerProxy.Maps;

public class ServerInfo {
    private final String serverName;
    private final String ip;
    private final String port;
    private final String password;

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