package de.voasis.serverHandlerProxy.Helper;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.concurrent.Callable;

public class PingUtil {
    static DataHolder dataHolder;
    static ProxyServer server;
    static Logger logger;
    static Object plugin;

    public PingUtil(DataHolder dataHolder, ProxyServer server, Logger logger, Object plugin) {
        PingUtil.dataHolder = dataHolder;
        PingUtil.server = server;
        PingUtil.logger = logger;
        PingUtil.plugin = plugin;
    }
    public void updateState() {
        for(BackendServer backendServer : dataHolder.backendInfoMap) {
            Optional<RegisteredServer> r = server.getServer(backendServer.getServerName());
            r.ifPresent(registeredServer -> pingServer(registeredServer, stateComplete(registeredServer), stateCompleteFailed(registeredServer), logger, plugin));
        }
    }
    public Callable<Void> stateComplete(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (!backendServer.getState()) {
                        backendServer.setState(true);
                        logger.info("Server: " + backendServer.getServerName() + ", is now online.");
                        CommandSource creator = backendServer.getCreator();
                        if(creator != null) {
                            creator.sendMessage(Component.text("Server: " + backendServer.getServerName() + ", is now online.", NamedTextColor.GREEN));
                        }
                    }
                }
            }
            return null;
        };
    }
    public Callable<Void> stateCompleteFailed(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (backendServer.getState()) {
                        backendServer.setState(false);
                        logger.info("Server: " + backendServer.getServerName() + ", is now offline.");
                    }
                }
            }
            return null;
        };
    }
    public void pingServer(RegisteredServer regServer, Callable<Void> response, Callable<Void> noResponse, Logger logger, Object plugin) {
        regServer.ping().whenComplete((result, exception) -> {
            if (exception == null) {
                try {
                    synchronized (plugin) {
                        response.call();
                    }
                } catch (Exception e) {
                    logger.error("Error while executing success response for server: " + regServer.getServerInfo().getName(), e);
                }
            } else {
                try {
                    synchronized (plugin) {
                        noResponse.call();
                    }
                } catch (Exception e) {
                    logger.error("Error while executing failure response for server: " + regServer.getServerInfo().getName(), e);
                }
            }
        });
    }
}
