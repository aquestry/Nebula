package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class PluginMessage {
    private final Logger logger = LoggerFactory.getLogger("nebula");
    public PluginMessage(PluginMessageEvent event, ProxyServer server) {
        String messageContent = new String(event.getData(), StandardCharsets.UTF_8);
        if (event.getIdentifier().equals(Nebula.channel)) {
            if (messageContent.startsWith("lobby:")) {
                logger.info("Sending to Lobby");
                Nebula.util.connectPlayer(server.getPlayer(messageContent.replace("lobby:", "")).get(), Nebula.defaultsManager.getTarget(), true);
            } else if (messageContent.startsWith("queue:")) {
                if (messageContent.split(":").length != 3) logger.warn("Incorrect queue plugin message format: {}", messageContent);
                Optional<Player> player = server.getPlayer(messageContent.split(":")[1]);
                if (player.isEmpty()) {
                    logger.info("Player {} not found", messageContent.split(":")[1]);
                    return;
                }
                Nebula.util.joinQueue(player.get(), messageContent.split(":")[2]);
            } else if (messageContent.startsWith("leave_queue:")) {
                if (messageContent.split(":").length != 2) logger.warn("Incorrect leave queue plugin message format: {}", messageContent);
                Optional<Player> player = server.getPlayer(messageContent.split(":")[1]);
                player.ifPresent(p -> Nebula.util.leaveQueue(p));
            }
        }
    }
}
