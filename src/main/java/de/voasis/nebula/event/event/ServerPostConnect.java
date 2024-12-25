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
        String score = player.getUsername() + ":<red>Test:<white>Line 1#<blue>Line 2#<gray>Line 3";
        server.sendPluginMessage(Nebula.channelScore, score.getBytes());
    }
}