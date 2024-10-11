package de.voasis.nebula.Commands;

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
        String reason = invocation.arguments().length > 0 ? invocation.arguments()[0] : "No reason provided";
        server.shutdown(Component.text("Shutdown! Reason: " + reason).color(NamedTextColor.WHITE));
    }


    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}