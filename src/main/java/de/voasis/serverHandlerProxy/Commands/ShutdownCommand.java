package de.voasis.serverHandlerProxy.Commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;


public class ShutdownCommand implements SimpleCommand {
    private ProxyServer server;
    public ShutdownCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        server.shutdown(Component.text("Shutdown! Reason:" + invocation.arguments()[0]).color(NamedTextColor.WHITE));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}