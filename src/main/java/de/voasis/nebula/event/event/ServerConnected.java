package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Nebula;

public class ServerConnected {
    public ServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        RegisteredServer server = player.getCurrentServer().get().getServer();
        String rankName = Nebula.permissionManager.getGroup(player).getPrefix();
        String serverName = Nebula.util.getBackendServer(server.getServerInfo().getName())
                .getFlags()
                .getFirst()
                .toLowerCase()
                .replace("gamemode:", "");
        serverName = serverName.substring(0, 1).toUpperCase() + serverName.substring(1);
        String score = player.getUsername() + "&<blue>Nebula&<reset>#<white>Rank: " + rankName + "#<white>Service: " + serverName + "#<reset>";
        server.sendPluginMessage(Nebula.channelScore, score.getBytes());
    }
}
