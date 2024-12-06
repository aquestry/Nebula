package de.voasis.nebula.map;

import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Party {

    private Player leader;
    private final List<Player> members = new ArrayList<>();
    private final Map<Player, Long> invites = new HashMap<>();

    public Party(Player leader) {
        this.leader = leader;
        this.members.add(leader);
    }

    public Player getLeader() { return leader; }
    public List<Player> getMembers() { return new ArrayList<>(members); }
    public boolean isMember(Player player) { return members.contains(player); }
    public boolean isInvited(Player player) { return invites.containsKey(player); }
    public void addMember(Player player) { if (!members.contains(player)) members.add(player); }
    public void removeMember(Player player) { members.remove(player); }
    public void removeInvite(Player player) { invites.remove(player); }
    public void addInvite(Player player) { if (!invites.containsKey(player)) invites.put(player, System.currentTimeMillis()); }
    public void newLeader(Player player) { leader = player; }
    public List<String> getInvites() { return invites.keySet().stream().map(Player::getUsername).toList(); }

    public void refreshInvites() {
        long currentTime = System.currentTimeMillis();
        invites.entrySet().removeIf(entry -> {
            boolean isExpired = currentTime - entry.getValue() > 60000;
            if (isExpired) {
                // Sned ran out mesages
                Nebula.util.log("Removing expired invite for player: {}", entry.getKey().getUsername());
            }
            return isExpired;
        });
    }
}