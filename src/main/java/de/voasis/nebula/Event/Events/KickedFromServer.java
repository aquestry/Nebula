package de.voasis.nebula.Event.Events;

import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;

public class KickedFromServer {
    public KickedFromServer(KickedFromServerEvent event, ProxyServer server) {
        if(event.getServer().getPlayersConnected().isEmpty()) {
            for (BackendServer backendServer : Nebula.dataHolder.backendInfoMap) {
                if(backendServer.getServerName().equals(event.getServer().getServerInfo().getName()) && !backendServer.getServerName().equals("default-0")) {
                    Nebula.externalServerManager.delete(backendServer.getHoldServer(), backendServer.getServerName(), server.getConsoleCommandSource());
                }
            }
        }
    }
}
