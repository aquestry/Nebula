package de.voasis.nebula.Event.Event;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Nebula;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class PluginMessage {
    public PluginMessage(PluginMessageEvent event, ProxyServer server) {
        String messageContent = new String(event.getData(), StandardCharsets.UTF_8);
        if (event.getIdentifier().equals(Nebula.channel)) {
            if (messageContent.startsWith("lobby:")) {
                Player player = server.getPlayer(messageContent.replace("lobby:", "")).get();
                Nebula.util.connectPlayer(player, Nebula.defaultsManager.getTarget(), true);
            } else if (messageContent.startsWith("queue:")) {
                if (messageContent.split(":").length != 3) Nebula.util.log("Incorrect queue plugin message format: {}", messageContent);
                Optional<Player> player = server.getPlayer(messageContent.split(":")[1]);
                if (player.isEmpty()) {
                    Nebula.util.log("Player {} not found", messageContent.split(":")[1]);
                    return;
                }
                Nebula.queueProcessor.joinQueue(player.get(), messageContent.split(":")[2]);
            } else if (messageContent.startsWith("leave_queue:")) {
                if (messageContent.split(":").length != 2) Nebula.util.log("Incorrect leave queue plugin message format: {}", messageContent);
                Optional<Player> player = server.getPlayer(messageContent.split(":")[1]);
                player.ifPresent(p -> Nebula.queueProcessor.leaveQueue(p));
            }
        }
    }
}
