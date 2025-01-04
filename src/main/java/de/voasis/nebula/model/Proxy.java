package de.voasis.nebula.model;

import de.voasis.nebula.Nebula;

public class Proxy {

    private final String name;
    private final String ip;
    private final int port;
    private boolean online;
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
    public void setOnline(boolean online) { this.online = online; Nebula.util.log("Proxy: " + getName() + " is " + (online ? "online" : "offline")); }
    public int getLevel() { return level; }
}