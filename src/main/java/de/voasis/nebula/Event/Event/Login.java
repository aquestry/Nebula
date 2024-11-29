package de.voasis.nebula.Event.Event;

import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Nebula;
import java.util.UUID;

public class Login {
    public Login(LoginEvent event, ProxyServer server) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (Data.adminUUIDs.contains(playerUUID.toString())) {
            Nebula.permissionManager.addPermission(player, "velocity.*");
        }
    }
}
