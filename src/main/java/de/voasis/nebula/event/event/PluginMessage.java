package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Data;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class PluginMessage {

    public PluginMessage(PluginMessageEvent event) {
        if(!(event.getSource() instanceof ServerConnection)) return;
        String messageContent = new String(event.getData(), StandardCharsets.UTF_8);
        if (event.getIdentifier().equals(Nebula.channel)) {
            event.setResult(PluginMessageEvent.ForwardResult.handled());
            String playerName = messageContent.contains(":") ? messageContent.split(":")[1] : "";
            long now = System.currentTimeMillis();
            if (Data.cooldownsPluginMessage.containsKey(playerName) && now - Data.cooldownsPluginMessage.get(playerName) < 1000) return;
            Data.cooldownsPluginMessage.put(playerName, now);
            if (messageContent.startsWith("lobby:")) {
                Player player = Nebula.server.getPlayer(messageContent.replace("lobby:", "")).get();
                Nebula.util.connectPlayer(player, Nebula.defaultsManager.getTarget(), true);
            } else if (messageContent.startsWith("queue:")) {
                if (messageContent.split(":").length != 3) Nebula.util.log("Incorrect queue plugin message format: {}", messageContent);
                Optional<Player> player = Nebula.server.getPlayer(playerName);
                if (player.isEmpty()) {
                    Nebula.util.log("Player {} not found", playerName);
                    return;
                }
                Nebula.queueProcessor.joinQueue(player.get(), messageContent.split(":")[2]);
            } else if (messageContent.startsWith("leave_queue:")) {
                if (messageContent.split(":").length != 2) Nebula.util.log("Incorrect leave queue plugin message format: {}", messageContent);
                Optional<Player> player = Nebula.server.getPlayer(playerName);
                player.ifPresent(p -> Nebula.queueProcessor.leaveQueue(p, true));
            }
        }
    }
}