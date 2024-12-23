package de.voasis.nebula.event.event;

import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.Component;

public class PlayerChat {
    public PlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        Component message = Nebula.mm.deserialize( Nebula.permissionManager.getGroup(player).getPrefix() + player.getUsername() + ": <reset>" + event.getMessage());
        for(Player p : event.getPlayer().getCurrentServer().get().getServer().getPlayersConnected()) {
            p.sendMessage(message);
        }
    }
}
