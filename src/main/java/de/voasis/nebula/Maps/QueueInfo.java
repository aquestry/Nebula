package de.voasis.nebula.Maps;

import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import java.util.List;

public class QueueInfo {
    private GamemodeInfo gamemode;
    private List<Player> players = new ArrayList<>();
    private boolean used = false;
    public QueueInfo(GamemodeInfo gamemode) {
        this.gamemode = gamemode;
    }

    public int getPlayerCount() {
        return players.size();
    }
    public boolean getUsed() {
        return used;
    }
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public GamemodeInfo getGamemode() {
        return gamemode;
    }
    public void setUsed(boolean used) {
        this.used = used;
    }
    public void addPlayer(Player player) {
        if (players.stream().noneMatch(p -> p.getUniqueId().equals(player.getUniqueId()))) {
            players.add(player);
        }
    }

    public void removePlayer(Player player) {
        players.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
    }
}
