package de.voasis.nebula.network;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.data.Messages;
import de.voasis.nebula.model.Container;
import java.util.ArrayList;
import java.util.Optional;

public class ContainerStateChecker {
    public void pingServers() {
        if(Config.quitting) return;
        Nebula.util.checkLobbys(false);
        for (Container container : new ArrayList<>(Config.backendInfoMap)) {
            Optional<RegisteredServer> registeredServer = Nebula.server.getServer(container.getServerName());
            registeredServer.ifPresent(regServer -> regServer.ping().whenComplete((result, exception) -> {
                if (exception == null) {
                    try {
                        stateComplete(regServer);
                    } catch (Exception e) {
                        Nebula.util.log("Error while executing success response for server: {}", regServer.getServerInfo().getName());
                    }
                } else {
                    try {
                        stateCompleteFailed(regServer);
                    } catch (Exception e) {
                        Nebula.util.log("Error while executing failure response for server: {}", regServer.getServerInfo().getName());
                    }
                }
            }));
        }
    }

    private void stateComplete(RegisteredServer registeredServer) {
        for (Container container : Config.backendInfoMap) {
            if (registeredServer.getServerInfo().getName().equals(container.getServerName())) {
                synchronized (container) {
                    if (!container.isOnline()) {
                        container.setOnline(true);
                        Nebula.util.callPending(container);
                        CommandSource creator = container.getCreator();
                        Nebula.util.sendMessage(creator, Messages.ONLINE.replace("<name>", container.getServerName()));
                    }
                }
            }
        }
    }

    public void stateCompleteFailed(RegisteredServer registeredServer) {
        for (Container container : Config.backendInfoMap) {
            if (registeredServer.getServerInfo().getName().equals(container.getServerName())) {
                synchronized (container) {
                    if (container.isOnline()) {
                        Nebula.util.checkLobbys(true);
                        container.setOnline(false);
                        CommandSource creator = container.getCreator();
                        Nebula.util.sendMessage(creator, Messages.OFFLINE.replace("<name>", container.getServerName()));
                    }
                }
            }
        }
    }
}
