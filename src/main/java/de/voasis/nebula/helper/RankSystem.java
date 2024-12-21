package de.voasis.nebula.helper;

import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import java.util.List;

public class RankSystem {

    private List<Player> admins = new ArrayList<>();

    public void makeAdmin(Player player) {
        admins.add(player);
    }

    public String getRank(Player player) {
        if (admins.contains(player)) {
            return "admin#1#<red>Admin";
        }
        return "player#2#<white>Player";
    }
}
