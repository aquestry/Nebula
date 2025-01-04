package de.voasis.nebula.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;

public class ProxyCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length < 1 || !Config.multiProxyMode) {
            return;
        }
        if(args[0].equalsIgnoreCase("nodes")) {
            Nebula.util.sendMessage(source, "Nodes: " + Nebula.multiProxySender.getNodes(Config.proxyMap.getFirst()));
        }
        if(args[0].equalsIgnoreCase("servers")) {
            Nebula.util.sendMessage(source, "Servers: " + Nebula.multiProxySender.getServers(Config.proxyMap.getFirst()));
        }
    }
    @Override
    public boolean hasPermission(SimpleCommand.Invocation invocation) {
        CommandSource sender = invocation.source();
        return sender.hasPermission("velocity.admin") || sender instanceof ConsoleCommandSource;
    }
}
