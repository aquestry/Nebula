package de.voasis.nebula.helper;

import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.data.Messages;
import de.voasis.nebula.map.Container;
import de.voasis.nebula.map.Queue;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.map.Party;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueueProcessor {

    public void init() {
        for (Queue queue : Data.queueMap) {
            int preloadCount = queue.getPreload();
            for (int i = 0; i < preloadCount; i++) {
                createPreloadedServer(queue);
            }
        }
    }

    public void process() {
        for (Queue queue : Data.queueMap) {
            int neededPlayers = queue.getNeededPlayers();
            if (queue.getInQueue().size() >= neededPlayers) {
                List<Player> playersToMove = new ArrayList<>();
                Party p = Nebula.partyManager.getParty(queue.getInQueue().getFirst());
                if(p != null) {
                    playersToMove = p.getMembers();
                    for(Player player : playersToMove) {
                        queue.getInQueue().remove(player);
                    }
                } else {
                    for (int i = 0; i < neededPlayers; i++) {
                        playersToMove.add(queue.getInQueue().removeFirst());
                    }
                }
                Optional<Container> preloadedServer = findPreloadedServer(queue);
                if (preloadedServer.isPresent()) {
                    Container server = preloadedServer.get();
                    server.removeFlag("preload");
                    for (Player player : playersToMove) {
                        server.addPendingPlayerConnection(player);
                    }
                    createPreloadedServer(queue);
                } else {
                    Container newServer = createNewServer(queue);
                    for (Player player : playersToMove) {
                        newServer.addPendingPlayerConnection(player);
                    }
                }
            }
        }
    }

    private Optional<Container> findPreloadedServer(Queue queue) {
        return Data.backendInfoMap.stream()
                .filter(server -> {
                    boolean hasGamemode = server.getFlags().contains("gamemode:" + queue.getName());
                    boolean hasPreload = server.getFlags().contains("preload");
                    boolean isOnline = server.isOnline();
                    return hasGamemode && hasPreload && isOnline;
                })
                .findFirst();
    }

    private void createPreloadedServer(Queue queue) {
        createNewServer(queue, "preload", "retry", "gamemode:" + queue.getName());
    }

    private Container createNewServer(Queue queue, String... flags) {
        String serverName = queue.getName() + "-" + Nebula.util.generateUniqueString();
        return Nebula.serverManager.createFromTemplate(
                queue.getTemplate(),
                serverName,
                Nebula.server.getConsoleCommandSource(),
                flags
        );
    }

    public void joinQueue(Player player, String queueName) {
        Container currentServer = Nebula.util.getBackendServer(
                player.getCurrentServer().get().getServerInfo().getName()
        );
        if (!currentServer.getFlags().contains("lobby")) {
            Nebula.util.sendMessage(player, Messages.LOBBY_ONLY);
            return;
        }
        if (isInAnyQueue(player)) {
            Nebula.util.sendMessage(player, Messages.ALREADY_IN_QUEUE);
            return;
        }
        Data.queueMap.stream()
                .filter(queue -> queue.getName().equalsIgnoreCase(queueName))
                .findFirst()
                .ifPresentOrElse(
                        queue -> {
                            Party playerParty = Nebula.partyManager.getParty(player);
                            if(playerParty != null) {
                                if(!playerParty.getLeader().equals(player)) {
                                    Nebula.util.sendMessage(player, Messages.PARTY_NOT_ALLOWED);
                                    return;
                                }
                                if(queue.getNeededPlayers() != playerParty.getMembers().size()) {
                                    Nebula.util.sendMessage(player, Messages.QUEUE_PLAYER_COUNT_MISMATCH);
                                    return;
                                }
                                playerParty.getMembers().forEach(member -> {
                                    if (!queue.getInQueue().contains(member)) {
                                        queue.getInQueue().add(member);
                                        Nebula.util.sendMessage(member, Messages.ADDED_TO_QUEUE.replace("<queue>", queueName));
                                    }
                                });
                            } else  {
                                queue.getInQueue().add(player);
                                Nebula.util.sendMessage(player, Messages.ADDED_TO_QUEUE.replace("<queue>", queueName));
                            }
                        },
                        () -> Nebula.util.sendMessage(player, Messages.QUEUE_NOT_FOUND)
                );
    }

    public void leaveQueue(Player player, boolean warn) {
        if (!isInAnyQueue(player)) {
            if(warn) { Nebula.util.sendMessage(player, Messages.NOT_IN_QUEUE); }
            return;
        }
        Party party = Nebula.partyManager.getParty(player);
        if(party != null) {
            if(!party.getLeader().equals(player)) {
                Nebula.util.sendMessage(player, Messages.PARTY_NOT_ALLOWED);
                return;
            }
        }
        for (Queue queue : Data.queueMap) {
            if (queue.getInQueue().remove(player)) {
                Nebula.util.sendMessage(player, Messages.REMOVED_FROM_QUEUE.replace("<queue>", queue.getName()));
            }
        }
    }

    public boolean isInAnyQueue(Player player) {
        return Data.queueMap.stream()
                .anyMatch(queue -> queue.getInQueue().contains(player));
    }
}