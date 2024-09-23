package de.voasis.serverHandlerProxy.Events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.ServerHandlerProxy;


public class PostLogin {
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        RegisteredServer temp = null;
        for (RegisteredServer i : ServerHandlerProxy.server.getAllServers()) {
            if(ServerHandlerProxy.dataHolder.defaultServer.equals(i.getServerInfo().getName())) {
                temp = i;
            }
        }
        if(temp != null) {
            event.getPlayer().createConnectionRequest(temp).connect();
        }
    }
}
