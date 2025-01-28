package dev.aquestry.nebula.model;

import dev.aquestry.nebula.Nebula;

public class Proxy {

    private final String name;
    private final String ip;
    private final int port;
    private boolean online;
    private int level;

    public Proxy(String name, String ip, int port, boolean online) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.online = online;
    }

    public String getName() { return name; }
    public String getIP() { return ip; }
    public int getPort() { return port; }
    public boolean isOnline() { return online; }
    public void setLevel(int level) { this.level = level; Nebula.util.log("Proxy: {} has a level of {}.", getName(), level); }
    public void setOnline(boolean online) { this.online = online; Nebula.util.log("Proxy: " + getName() + " is " + (online ? "online" : "offline")); }
    public int getLevel() { return level; }
}