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

    public Proxy(String name, String ip, int port, int level) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.level = level;
    }

    public String getName() { return name; }
    public String getIP() { return ip; }
    public int getPort() { return port; }
    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; Nebula.util.log("Proxy: " + getName() + " marked " + (online ? "online" : "offline")); }
    public Socket getSocket() { return socket; }
    public void setSocket(Socket socket) { this.socket = socket; }
    public int getLevel() { return level; }
}