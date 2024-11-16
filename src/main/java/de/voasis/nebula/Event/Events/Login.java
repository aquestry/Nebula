package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public class Login {
    private final Logger logger = LoggerFactory.getLogger("nebula");
    public Login(LoginEvent event, ProxyServer server) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (Data.adminUUIDs.contains(playerUUID.toString())) {
            Nebula.permissionManager.addPermission(player, "velocity.*");
        }
        logger.info("User logged in: {}, UUID: {}, is admin : {}.",
                player.getUsername(), playerUUID,
                player.hasPermission("velocity.admin"));
    }
}
