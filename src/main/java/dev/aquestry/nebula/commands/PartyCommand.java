package dev.aquestry.nebula.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.data.Messages;
import java.util.List;

public class PartyCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();
        if (invocation.source() instanceof Player player) {
            if (args.length >= 1) {
                switch (args[0].toLowerCase()) {
                    case "accept":
                        if (args.length >= 2) {
                            Nebula.partyManager.tryJoin(player, args[1]);
                        } else {
                            if(!Nebula.partyManager.getAllInvites(player).isEmpty()) {
                                Nebula.partyManager.tryJoin(player, Nebula.partyManager.getAllInvites(player).getFirst());
                            } else {
                                sendUsageMessage(player);
                            }
                        }
                        break;
                    case "invite":
                        if (args.length >= 2) {
                            Nebula.partyManager.inviteCommand(player, args[1]);
                        } else {
                            sendUsageMessage(player);
                        }
                        break;
                    case "leave":
                        Nebula.partyManager.quit(player);
                        break;
                    default:
                        sendUsageMessage(player);
                        break;
                }
            } else {
                sendUsageMessage(player);
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (invocation.source() instanceof Player player) {
            if (args.length == 0) {
                return List.of("accept", "invite", "leave");
            }
            if (args.length == 1) {
                return List.of("accept", "invite", "leave").stream()
                        .filter(subcommand -> subcommand.toLowerCase().startsWith(args[0].toLowerCase()))
                        .toList();
            }
            if (args.length == 2 && "invite".equalsIgnoreCase(args[0])) {
                return Nebula.server.getAllPlayers().stream()
                        .filter(p -> Nebula.partyManager.getParty(p) == null)
                        .filter(p -> p.getUsername().toLowerCase().startsWith(args[1].toLowerCase()))
                        .filter(p -> !Nebula.partyManager.isInvitedInMyParty(player, p))
                        .filter(p -> !p.equals(player))
                        .map(Player::getUsername)
                        .toList();
            }
            if (args.length == 2 && "accept".equalsIgnoreCase(args[0])) {
                return Nebula.partyManager.getAllInvites(player);
            }
        }
        return List.of();
    }

    private void sendUsageMessage(CommandSource source) {
        Nebula.util.sendMessage(source, Messages.USAGE_PARTY);
    }
}