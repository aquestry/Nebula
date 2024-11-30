package de.voasis.nebula.map;

import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import java.util.List;

public class GamemodeQueue {

    private final String name;
    private final String template;
    private final int neededPlayers;
    private final int preload;
    private List<Player> inQueue = new ArrayList<>();

    public GamemodeQueue(String name, String template, int neededPlayers, int preload) {
        this.name = name;
        this.template = template;
        this.neededPlayers = neededPlayers;
        this.preload = preload;
    }

    public String getName() { return name; }
    public String getTemplate() { return template; }
    public int getNeededPlayers() { return neededPlayers; }
    public List<Player> getInQueue() { return inQueue; }
    public int getPreload() { return preload; }
}
