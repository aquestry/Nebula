package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Helper.DataHolder;
import de.voasis.nebula.Helper.Messages;
import de.voasis.nebula.Permission.PermissionManager;
import org.slf4j.Logger;

public class Login {
    public Login(LoginEvent event, DataHolder dataHolder, PermissionManager permissionManager, Logger logger) {
        Player player = event.getPlayer();
        if(dataHolder.admins.contains(player.getUniqueId().toString())) {
            for(String perm : Messages.adminRights) {
                permissionManager.addPermission(player, perm);
            }
        }
        logger.info("Player is admin: {}", player.hasPermission("velocity.admin"));
    }
}
