package de.voasis.nebula.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import de.voasis.nebula.Nebula;

public class GroupCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        Nebula.permissionFile.reloadGroups();
        if (args.length < 1) {
            return;
        }
        if(args[0].equalsIgnoreCase("reload")) {
            Nebula.permissionFile.reloadGroups();
            Nebula.multiProxySender.sendGroups();
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        CommandSource sender = invocation.source();
        return sender.hasPermission("velocity.admin") || sender instanceof ConsoleCommandSource;
    }
}