package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Messages;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ShutdownCommand implements SimpleCommand {
    private final ProxyServer server;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ShutdownCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String reason = invocation.arguments().length > 0 ? String.join(" ", invocation.arguments()) : "No reason provided";
        server.shutdown(mm.deserialize(Messages.FEEDBACK_SHUTDOWN.replace("<reason>", reason)));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}
