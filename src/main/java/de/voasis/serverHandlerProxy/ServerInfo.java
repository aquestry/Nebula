package de.voasis.serverHandlerProxy;

public class ServerInfo {
    private final String serverName;
    private final String ip;
    private final int port;
    private final String password;

    public ServerInfo(String ip, int port, String password) {
        this.serverName = ip;
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

    public int getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }
}