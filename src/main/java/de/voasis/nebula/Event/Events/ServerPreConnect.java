package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ServerPreConnect {
    public ServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer target = event.getOriginalServer();
        if(!Nebula.dataHolder.getBackendServer(target.getServerInfo().getName()).isOnline()) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            player.sendMessage(Component.text("The server you are trying to connect to is offline", NamedTextColor.GOLD));
        }
        if(player.getCurrentServer().equals(target)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            player.sendMessage(Component.text("You are already connected to that server.", NamedTextColor.GOLD));
        }
    }
}
