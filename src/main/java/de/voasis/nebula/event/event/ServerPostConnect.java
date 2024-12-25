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
        String serverName = server.getServerInfo().getName();
        String score = player.getUsername() + ":<blue>Nebula:<white>-----#<white>Rank " + rankName + "#<white>Server: " + serverName + "#<white>-----";
        System.out.println("Sending Scoreboard: " + score);
        server.sendPluginMessage(Nebula.channelScore, score.getBytes());
    }
}