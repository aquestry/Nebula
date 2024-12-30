package de.voasis.nebula.map;

import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import java.util.List;

public class Queue {

    private final String name;
    private final String template;
    private final int neededPlayers;
    private final int preload;
    private String localEnvVars;
    private List<Player> inQueue = new ArrayList<>();

    public Queue(String name, String template, int neededPlayers, int preload, String localEnvVars) {
        this.name = name;
        this.template = template;
        this.neededPlayers = neededPlayers;
        this.preload = preload;
        this.localEnvVars = localEnvVars;
    }

    public String getLocalEnvVars() { return localEnvVars; }
    public String getName() { return name; }
    public String getTemplate() { return template; }
    public int getNeededPlayers() { return neededPlayers; }
    public List<Player> getInQueue() { return inQueue; }
    public int getPreload() { return preload; }
}