package de.voasis.nebula.model;

import de.voasis.nebula.Nebula;
import java.net.Socket;

public class Proxy {

    private final String name;
    private final String ip;
    private final int port;
    private boolean online;
    private Socket socket;
    private final int level;

    public Proxy(String name, String ip, int port, int level, boolean online) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.level = level;
        this.online = online;
    }

    public String getName() { return name; }
    public String getIP() { return ip; }
    public int getPort() { return port; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; Nebula.util.log("Proxy: " + getName() + " is now " + (online ? "online" : "offline")); Nebula.multiProxyServer.recheckMaster(); }
    public Socket getSocket() { return socket; }
    public void setSocket(Socket socket) { this.socket = socket; }
    public int getLevel() { return level; }
}