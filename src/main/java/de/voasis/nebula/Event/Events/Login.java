package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;

public class Login {
    public Login(LoginEvent event, Logger logger) {
        Player player = event.getPlayer();
        if (Data.adminUUIDs.contains(player.getUniqueId())) {
            Nebula.permissionManager.addPermission(player, "velocity.admin");
            logger.info("User logged in: {}, is admin : {}.", player.getUsername(), player.hasPermission("velocity.admin"));
        }
    }
}
