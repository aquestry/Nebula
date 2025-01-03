package de.voasis.nebula.model;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import java.util.List;

public class Container {

    private final Node node;
    private final int port;
    private final String serverName;
    private final CommandSource creator;
    private final String template;
    private boolean online;
    private final List<Player> pendingPlayerConnections = new ArrayList<>();
    public List<String> flags = new ArrayList<>();

    public Container(String serverName, Node node, int port, boolean online, CommandSource creator, String template, String... starterFlags) {
        this.serverName = serverName;
        this.node = node;
        this.port = port;
        this.online = online;
        this.creator = creator;
        this.template = template;
        this.flags.addAll(List.of(starterFlags));
    }

    public String getServerName() { return serverName; }
    public int getPort() { return port; }
    public Node getHoldServer() { return node; }
    public boolean isOnline() { return online; }
    public CommandSource getCreator() { return creator; }
    public void addPendingPlayerConnection(Player player) { if (!pendingPlayerConnections.contains(player)) { pendingPlayerConnections.add(player);} }
    public List<Player> getPendingPlayerConnections() { return new ArrayList<>(pendingPlayerConnections); }
    public void removePendingPlayerConnection(Player player) { pendingPlayerConnections.remove(player); }
    public void setOnline(boolean online) { this.online = online; }
    public void removeFlag(String flag) { this.flags.remove(flag); }
    public List<String> getFlags() { return new ArrayList<>(flags); }
}