package de.voasis.nebula.Maps;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class BackendServer {

    private static final Logger logger = LoggerFactory.getLogger("nebula");
    private final HoldServer holdServer;
    private final int port;
    private final String serverName;
    private final CommandSource creator;
    private final String template;
    private boolean online;
    private final List<Player> pendingPlayerConnections = new ArrayList<>();
    private final String tag;

    public BackendServer(String serverName, HoldServer holdServer, int port, boolean online, CommandSource creator, String template, String tag) {
        this.serverName = serverName;
        this.holdServer = holdServer;
        this.port = port;
        this.online = online;
        this.creator = creator;
        this.template = template;
        this.tag = tag;
    }
    public String getServerName() { return serverName; }
    public String getTemplate() { return template; }
    public int getPort() { return port; }
    public HoldServer getHoldServer() { return holdServer; }
    public boolean isOnline() { return online; }
    public CommandSource getCreator() { return creator; }
    public void addPendingPlayerConnection(Player player) { if (!pendingPlayerConnections.contains(player)) {pendingPlayerConnections.add(player);} }
    public List<Player> getPendingPlayerConnections() { return new ArrayList<>(pendingPlayerConnections); }
    public void removePendingPlayerConnection(Player player) { pendingPlayerConnections.remove(player); }
    public void setOnline(boolean online) { this.online = online; }
    public String getTag() { return this.tag; }
}
