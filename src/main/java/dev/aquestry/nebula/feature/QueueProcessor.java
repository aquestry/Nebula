package dev.aquestry.nebula.feature;

import com.velocitypowered.api.proxy.Player;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.data.Messages;
import dev.aquestry.nebula.model.Container;
import dev.aquestry.nebula.model.Queue;
import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.model.Party;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueueProcessor {

    public QueueProcessor() {
        for (Queue queue : Config.queueMap) {
            int preloadCount = queue.getPreload();
            for (int i = 0; i < preloadCount; i++) {
                createPreloadedServer(queue);
            }
        }
    }

    public void process() {
        for (Queue queue : Config.queueMap) {
            int neededPlayers = queue.getNeededPlayers();
            if (queue.getInQueue().size() >= neededPlayers) {
                List<Player> playersToMove = new ArrayList<>();
                Optional<Party> p = Nebula.partyManager.getParty(queue.getInQueue().getFirst());
                if(p.isPresent()) {
                    playersToMove = p.get().getMembers();
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
        return Config.containerMap.stream()
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
        return Nebula.containerManager.createFromTemplate(
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
        Config.queueMap.stream()
                .filter(queue -> queue.getName().equalsIgnoreCase(queueName))
                .findFirst()
                .ifPresentOrElse(
                        queue -> {
                            Optional<Party> playerParty = Nebula.partyManager.getParty(player);
                            if(playerParty.isPresent()) {
                                if(!playerParty.get().getLeader().equals(player)) {
                                    Nebula.util.sendMessage(player, Messages.PARTY_NOT_ALLOWED);
                                    return;
                                }
                                if(queue.getNeededPlayers() != playerParty.get().getMembers().size()) {
                                    Nebula.util.sendMessage(player, Messages.QUEUE_PLAYER_COUNT_MISMATCH);
                                    return;
                                }
                                playerParty.get().getMembers().forEach(member -> {
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
        process();
    }

    public void leaveQueue(Player player, boolean warn) {
        if (!isInAnyQueue(player) && warn) {
            Nebula.util.sendMessage(player, Messages.NOT_IN_QUEUE);
            return;
        }
        Optional<Party> party = Nebula.partyManager.getParty(player);
        if(party.isPresent()) {
            if(!party.get().getLeader().equals(player)) {
                Nebula.util.sendMessage(player, Messages.PARTY_NOT_ALLOWED);
                return;
            }
        }
        for (Queue queue : Config.queueMap) {
            if (queue.getInQueue().remove(player)) {
                Nebula.util.sendMessage(player, Messages.REMOVED_FROM_QUEUE.replace("<queue>", queue.getName()));
            }
        }
    }

    public boolean isInAnyQueue(Player player) {
        return Config.queueMap.stream()
                .anyMatch(queue -> queue.getInQueue().contains(player));
    }
}