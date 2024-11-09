package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;

public class PluginMessage {
    private final Logger logger = LoggerFactory.getLogger("nebula");
    public PluginMessage(PluginMessageEvent event, ProxyServer server) {
        String messageContent = new String(event.getData(), StandardCharsets.UTF_8);
        logger.info("Message received: {}", messageContent);
        if (event.getIdentifier().equals(Nebula.channel)) {
            logger.info("Right Channel");
            if (messageContent.startsWith("lobby:")) {
                logger.info("Sending to Lobby");
                Nebula.util.connectPlayer(server.getPlayer(messageContent.replace("lobby:", "")).get(), Nebula.defaultsManager.getTarget(), true);
            }
        }
    }
}
