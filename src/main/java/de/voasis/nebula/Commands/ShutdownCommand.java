package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.nebula.Data.Messages;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ShutdownCommand implements SimpleCommand {

    private final ProxyServer server;
    private MiniMessage mm = MiniMessage.miniMessage();


    public ShutdownCommand(ProxyServer server) {
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        String reason =  "";
        if( invocation.arguments().length > 0) {
            reason = String.join(" ", invocation.arguments());
        }
        server.shutdown(mm.deserialize(Messages.SHUTDOWN.replace("<reason>", reason)));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}