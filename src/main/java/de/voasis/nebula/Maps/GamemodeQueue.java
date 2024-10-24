package de.voasis.nebula.Maps;

import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import java.util.List;

public class GamemodeQueue {

    private final String name;
    private final String template;
    private final int neededPlayers;
    private List<String> inQueue = new ArrayList<>();

    public GamemodeQueue(String name, String template, int neededPlayers) {
        this.name = name;
        this.template = template;
        this.neededPlayers = neededPlayers;
    }

    public String getName() { return name; }
    public String getTemplate() { return template; }
    public int getNeededPlayers() { return neededPlayers; }
    public List<String> getInQueue() { return inQueue; }
    public void addInQueue(Player player) { inQueue.add(player.getUniqueId().toString()); }
    public void removeInQueue(Player player) { inQueue.remove(player); }
}
