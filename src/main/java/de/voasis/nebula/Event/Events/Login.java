package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Nebula;
import org.slf4j.Logger;

public class Login {
    public Login(PostLoginEvent event, Logger logger) {
        Player player = event.getPlayer();
        if(Data.adminUUIDs.contains(player.getUniqueId().toString())) {
            Nebula.permissionManager.addPermission(player, "velocity.*");
        }
        logger.info("{} is admin: {} and has client brand: {}.", player.getUsername(), player.hasPermission("velocity.admin"), player.getClientBrand());
    }
}
