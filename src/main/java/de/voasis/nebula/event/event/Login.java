package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.Nebula;
import java.util.UUID;

public class Login {
    public Login(LoginEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (Data.adminUUIDs.contains(playerUUID.toString())) {
            Nebula.permissionManager.addPermission(player, "velocity.*");
        }
    }
}