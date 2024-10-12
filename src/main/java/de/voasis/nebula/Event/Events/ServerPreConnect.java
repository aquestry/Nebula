package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Helper.DataHolder;
import de.voasis.nebula.Helper.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ServerPreConnect {
    public ServerPreConnect(ServerPreConnectEvent event, DataHolder dataHolder) {
        Player player = event.getPlayer();
        RegisteredServer target = event.getOriginalServer();
        if(!dataHolder.getBackendServer(target.getServerInfo().getName()).isOnline()) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            player.sendMessage(Component.text(Messages.offline, NamedTextColor.GOLD));
        }
        if(player.getCurrentServer().equals(target)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            player.sendMessage(Component.text(Messages.already, NamedTextColor.GOLD));
        }
    }
}
