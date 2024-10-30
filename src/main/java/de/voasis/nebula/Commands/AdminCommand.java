package de.voasis.nebula.Commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Nebula;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class AdminCommand implements SimpleCommand {
    private final MiniMessage mm = MiniMessage.miniMessage();

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length < 1) {
            source.sendMessage(mm.deserialize(Messages.USAGE_ADMIN));
            return;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "kill":
                if (args.length < 2) {
                    source.sendMessage(mm.deserialize(Messages.USAGE_ADMIN_KILL));
                    return;
                }
                handleKillCommand(source, args);
                break;
            case "delete":
                if (args.length < 2) {
                    source.sendMessage(mm.deserialize(Messages.USAGE_ADMIN_DELETE));
                    return;
                }
                handleDeleteCommand(source, args);
                break;
            case "template":
                if (args.length < 3) {
                    source.sendMessage(mm.deserialize(Messages.USAGE_ADMIN_TEMPLATE));
                    return;
                }
                handleTemplateCommand(source, args);
                break;
            default:
                source.sendMessage(mm.deserialize(Messages.USAGE_UNKNOWN_COMMAND));
        }
    }

    private void handleKillCommand(CommandSource source, String[] args) {
        BackendServer backendServer = Nebula.util.getBackendServer(args[1]);
        if (backendServer != null) {
            source.sendMessage(mm.deserialize(Messages.FEEDBACK_KILL_START));
            Nebula.serverManager.kill(backendServer, source);
        } else {
            source.sendMessage(mm.deserialize(Messages.ERROR_SERVER_NOT_FOUND));
        }
    }

    private void handleDeleteCommand(CommandSource source, String[] args) {
        BackendServer backendServer = Nebula.util.getBackendServer(args[1]);
        if (backendServer != null) {
            source.sendMessage(mm.deserialize(Messages.FEEDBACK_DELETE_START));
            Nebula.serverManager.delete(backendServer, source);
        } else {
            source.sendMessage(mm.deserialize(Messages.ERROR_SERVER_NOT_FOUND));
        }
    }

    private void handleTemplateCommand(CommandSource source, String[] args) {
        String templateName = args[1];
        String newName = args[2];
        if (Nebula.util.getBackendServer(newName) == null) {
            source.sendMessage(mm.deserialize(Messages.FEEDBACK_TEMPLATE_CREATE));
            Nebula.serverManager.createFromTemplate(templateName, newName, source, "custom");
        } else {
            source.sendMessage(mm.deserialize(Messages.FEEDBACK_TEMPLATE_EXISTS));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("velocity.admin");
    }
}
