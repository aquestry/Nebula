package de.voasis.nebula.helper;

import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import java.util.List;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Messages;
import de.voasis.nebula.map.Party;

public class PartyManager {

    private final List<Party> parties = new ArrayList<>();

    public void refresh() {
        parties.forEach(Party::refreshInvites);
    }

    public void inviteCommand(Player player, String targetName) {
        Player target = Nebula.server.getPlayer(targetName).orElse(null);
        if (target == null) {
            Nebula.util.sendMessage(player, Messages.TARGET_INVITE_NOT_FOUND.replace("<target>", targetName));
            return;
        }

        if (isInParty(player)) {
            if (getParty(player).isInvited(target)) {
                Nebula.util.sendMessage(player, Messages.TARGET_INVITE_ALREADY.replace("<target>", targetName));
                return;
            }
            Nebula.util.sendMessage(player, Messages.SENT_INVITE.replace("<target>", targetName));
            Nebula.util.sendMessage(target, Messages.INVITED_MESSAGE.replace("<leader>", player.getUsername()));
            getParty(player).addInvite(target);
        } else {
            Party party = new Party(player);
            party.addInvite(target);
            Nebula.util.sendMessage(player, Messages.SENT_INVITE.replace("<target>", targetName));
            Nebula.util.sendMessage(target, Messages.INVITED_MESSAGE.replace("<leader>", player.getUsername()));
            parties.add(party);
        }
    }

    public void tryJoin(Player player, String targetName) {
        Player target = Nebula.server.getPlayer(targetName).orElse(null);
        if (target == null) {
            Nebula.util.sendMessage(player, Messages.NO_INVITE_FROM_LEADER.replace("<leader>", targetName));
            return;
        }
        if (!isInParty(target)) {
            Nebula.util.sendMessage(player, Messages.NO_INVITE_FROM_LEADER.replace("<leader>", targetName));
            return;
        }
        if (isInParty(player)) {
            Nebula.util.sendMessage(player, Messages.ALREADY_IN_PARTY);
            return;
        }
        Party party = getParty(target);
        if (party.isInvited(player)) {
            for(Player member : party.getMembers()) {
                Nebula.util.sendMessage(member, Messages.JOINED_PARTY.replace("<leader>", targetName).replace("<player>", player.getUsername()));
            }
            party.removeInvite(player);
            party.addMember(player);
        } else {
            Nebula.util.sendMessage(player, Messages.NO_INVITE_FROM_LEADER.replace("<leader>", targetName));
        }
    }

    public Party getParty(Player player) {
        return parties.stream()
                .filter(p -> p.getMembers().contains(player))
                .findFirst()
                .orElse(null);
    }

    public boolean isInParty(Player player) {
        return parties.stream().anyMatch(p -> p.isMember(player));
    }

    public boolean isInvitedInMyParty(Player player, Player target) {
        if (isInParty(player)) {
            return getParty(player).isInvited(target);
        }
        return false;
    }

    public List<String> getAllInvites(Player player) {
        return parties.stream()
                .filter(party -> party.isInvited(player))
                .map(Party::getLeader)
                .map(Player::getUsername)
                .toList();
    }

    public void quit(Player player) {
        Party party = getParty(player);
        if (party != null) {
            party.removeMember(player);
            Nebula.util.sendMessage(player, Messages.LEFT_PARTY.replace("<leader>", party.getLeader().getUsername()).replace("<player>", player.getUsername()));
            if (party.getMembers().size() <= 1) {
                parties.remove(party);
                return;
            }
            if (party.getLeader().equals(player)) {
                Player newLeader = party.getMembers().getFirst();
                party.newLeader(newLeader);
            } else {
                Nebula.util.sendMessage(player, Messages.LEFT_PARTY.replace("<leader>", party.getLeader().getUsername()).replace("<player>", player.getUsername()));
            }
        } else {
            Nebula.util.sendMessage(player, Messages.NO_PARTY_TO_LEAVE);
        }
    }
}