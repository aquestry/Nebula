package dev.aquestry.nebula.feature;

import com.velocitypowered.api.proxy.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.data.Messages;
import dev.aquestry.nebula.model.Party;

public class PartyManager {

    private final List<Party> parties = new ArrayList<>();

    public void refresh() {
        parties.forEach(Party::refreshInvites);
    }

    public void inviteCommand(Player player, String targetName) {
        Player target = Nebula.server.getPlayer(targetName).orElse(null);
        if (target == null || target.equals(player)) {
            Nebula.util.sendMessage(player, Messages.TARGET_INVITE_NOT_FOUND.replace("<target>", targetName));
            return;
        }
        Optional<Party> partyOpt = getParty(player);
        if (partyOpt.isPresent()) {
            Party party = partyOpt.get();
            if (party.isInvited(target)) {
                Nebula.util.sendMessage(player, Messages.TARGET_INVITE_ALREADY.replace("<target>", targetName));
                return;
            }
            if (!party.getLeader().equals(player)) {
                Nebula.util.sendMessage(player, Messages.PARTY_NOT_ALLOWED);
                return;
            }
            party.addInvite(target);
        } else {
            Party newParty = new Party(player);
            newParty.addInvite(target);
            parties.add(newParty);
        }
        Nebula.util.sendMessage(player, Messages.SENT_INVITE.replace("<target>", targetName));
        Nebula.util.sendMessage(target, Messages.INVITED_MESSAGE.replace("<leader>", player.getUsername()));
    }

    public void tryJoin(Player player, String targetName) {
        Player target = Nebula.server.getPlayer(targetName).orElse(null);
        if (target == null) {
            Nebula.util.sendMessage(player, Messages.NO_INVITE_FROM_LEADER.replace("<leader>", targetName));
            return;
        }
        Optional<Party> targetPartyOpt = getParty(target);
        if (targetPartyOpt.isEmpty()) {
            Nebula.util.sendMessage(player, Messages.NO_INVITE_FROM_LEADER.replace("<leader>", targetName));
            return;
        }
        Optional<Party> playerPartyOpt = getParty(player);
        if (playerPartyOpt.isPresent() && playerPartyOpt.get().getMembers().size() > 1) {
            Nebula.util.sendMessage(player, Messages.ALREADY_IN_PARTY);
            return;
        }
        Party targetParty = targetPartyOpt.get();
        if (targetParty.isInvited(player)) {
            targetParty.removeInvite(player);
            targetParty.addMember(player);
            Nebula.util.sendMemberMessage(targetParty, Messages.JOINED_PARTY.replace("<leader>", targetName)
                    .replace("<player>", player.getUsername()));
        } else {
            Nebula.util.sendMessage(player, Messages.NO_INVITE_FROM_LEADER.replace("<leader>", targetName));
        }
    }

    public Optional<Party> getParty(Player player) {
        return parties.stream()
                .filter(p -> p.isMember(player))
                .findFirst();
    }

    public boolean isInvitedInMyParty(Player player, Player target) {
        return getParty(player)
                .map(party -> party.isInvited(target))
                .orElse(false);
    }

    public List<String> getAllInvites(Player player) {
        return parties.stream()
                .filter(party -> party.isInvited(player))
                .map(Party::getLeader)
                .map(Player::getUsername)
                .toList();
    }

    public void quit(Player player) {
        Optional<Party> partyOpt = getParty(player);
        if (partyOpt.isPresent()) {
            Party party = partyOpt.get();
            party.getMembers().forEach(member -> Nebula.queueProcessor.leaveQueue(member, false));
            Nebula.util.sendMemberMessage(party, Messages.LEFT_PARTY
                    .replace("<leader>", party.getLeader().getUsername())
                    .replace("<player>", player.getUsername()));
            party.removeMember(player);
            if (party.getMembers().size() <= 1) {
                parties.remove(party);
            } else if (party.getLeader().equals(player)) {
                Player newLeader = party.getMembers().getFirst();
                party.newLeader(newLeader);
            }
        } else {
            Nebula.util.sendMessage(player, Messages.NO_PARTY_TO_LEAVE);
        }
        if (Nebula.queueProcessor.isInAnyQueue(player)) {
            Nebula.queueProcessor.leaveQueue(player, false);
        }
    }
}