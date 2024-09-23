package de.voasis.serverHandlerProxy.Events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import de.voasis.serverHandlerProxy.ServerHandlerProxy;

public class PostLogin {
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        event.getPlayer().createConnectionRequest(ServerHandlerProxy.dataHolder.defaultServer.get()).connect();
    }
}
