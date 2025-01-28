package dev.aquestry.nebula.event.events;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;
import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.data.Config;
import java.nio.charset.StandardCharsets;

public class PluginMessage {
    public PluginMessage(PluginMessageEvent event) {
        if (!(event.getSource() instanceof ServerConnection)) return;
        String message = new String(event.getData(), StandardCharsets.UTF_8);
        if (!event.getIdentifier().equals(Nebula.channelMain)) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());
        String[] parts = message.split(":");
        if (parts.length < 2) return;
        String action = parts[0];
        String playerName = parts[1];
        long now = System.currentTimeMillis();
        if (Config.cooldownsPluginMessage.getOrDefault(playerName, 0L) + 1000 > now) return;
        Config.cooldownsPluginMessage.put(playerName, now);
        switch (action) {
            case "lobby" ->
                    Nebula.server.getPlayer(playerName).ifPresent(player ->
                            Nebula.util.connectPlayer(player, Nebula.defaultsManager.getTarget(), true));
            case "queue" -> {
                if (parts.length == 3) {
                    Nebula.server.getPlayer(playerName).ifPresentOrElse(
                            player -> Nebula.queueProcessor.joinQueue(player, parts[2]),
                            () -> Nebula.util.log("Player {} not found", playerName));
                } else {
                    Nebula.util.log("Incorrect queue plugin message format: {}", message);
                }
            }
            case "leave_queue" -> {
                if (parts.length == 2) {
                    Nebula.server.getPlayer(playerName).ifPresent(player ->
                            Nebula.queueProcessor.leaveQueue(player, true));
                } else {
                    Nebula.util.log("Incorrect leave queue plugin message format: {}", message);
                }
            }
            default -> Nebula.util.log("Unknown plugin message action: {}", action);
        }
    }
}