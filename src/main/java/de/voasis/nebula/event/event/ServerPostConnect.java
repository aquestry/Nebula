package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Nebula;

public class ServerPostConnect {
    public ServerPostConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer server = player.getCurrentServer().get().getServer();
        Nebula.util.getBackendServer(server.getServerInfo().getName()).removePendingPlayerConnection(player);
        server.sendPluginMessage(Nebula.channelMain, Nebula.permissionManager.getGroupInfo(player).getBytes());
        String rankName = Nebula.permissionManager.getGroup(player).getPrefix();
        String serverName = Nebula.util.getBackendServer(server.getServerInfo().getName())
                .getFlags()
                .getFirst()
                .toLowerCase();
        serverName = serverName.substring(0, 1).toUpperCase() + serverName.substring(1);
        String score = player.getUsername() + "&<blue>Nebula&<white>-----#<white>Rank: " + rankName + "#<white>Service: " + serverName + "#<white>-----";
        server.sendPluginMessage(Nebula.channelScore, score.getBytes());
    }
}