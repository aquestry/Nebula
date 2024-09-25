package de.voasis.serverHandlerProxy.Maps;

public class BackendServer {
    private String holdServer;
    private int port;
    private String serverName;
    private boolean online;

    public BackendServer(String name, String holdServer, int port, boolean online) {
        this.serverName = name;
        this.port = port;
        this.holdServer = holdServer;
        this.online = online;
    }
    public String getServerName() {
        return serverName;
    }
    public int getPort() {
        return port;
    }
    public String getHoldServer() {
        return holdServer;
    }
    public boolean getState() {
        return online;
    }
    public void setState(boolean online) {
        this.online = online;
    }
}
