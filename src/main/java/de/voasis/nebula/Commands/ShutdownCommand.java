package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShutdownCommand implements SimpleCommand {
    private final ProxyServer server;
    public ShutdownCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        String reason =  "No reason provided";
        if( invocation.arguments().length > 0) {
            reason = String.join(" ", invocation.arguments());
        }
        server.shutdown(Component.text("Shutdown! Reason: " + reason).color(NamedTextColor.WHITE));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}