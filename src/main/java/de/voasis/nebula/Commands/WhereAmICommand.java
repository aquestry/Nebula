package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;

public class WhereAmICommand implements SimpleCommand {

    private ProxyServer server;

    public WhereAmICommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        if(source instanceof Player player) {
            for(RegisteredServer registeredServer : server.getAllServers()) {
                for(BackendServer backendServer : Data.backendInfoMap) {
                    String name = backendServer.getServerName();
                    if(registeredServer.getServerInfo().getName().equals(name)) {
                        Nebula.util.sendMessage(source ,"You on the backend server " + name + " on hold server " + backendServer.getHoldServer().getServerName() + ".");
                    }
                }
            }
        }
    }
}
