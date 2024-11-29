package de.voasis.nebula;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Map.BackendServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Map.HoldServer;
import net.kyori.adventure.text.Component;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerManager {

    private final ProxyServer server;

    public ServerManager(ProxyServer proxyServer) {
        this.server = proxyServer;
    }

    private void executeSSHCommand(HoldServer externalServer, String command, Runnable onSuccess, Runnable onError) {
        Session session = null;
        ChannelExec channelExec = null;
        try {
            session = new JSch().getSession(externalServer.getUsername(), externalServer.getIp(), 22);
            session.setPassword(externalServer.getPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.connect();
            while (!channelExec.isClosed()) {
                Thread.sleep(100);
            }
            boolean success = channelExec.getExitStatus() == 0;
            if (success) {
                onSuccess.run();
            } else {
                onError.run();
            }
        } catch (Exception e) {
            onError.run();
        } finally {
            if (channelExec != null) channelExec.disconnect();
            if (session != null) session.disconnect();
        }
    }

    public BackendServer createFromTemplate(String templateName, String newName, CommandSource source, String... starterFlags) {
        try {
            HoldServer externalServer = Data.holdServerMap.getFirst();
            for(HoldServer holdServer : Data.holdServerMap) {
                if(holdServer.getBackendServers().size() < externalServer.getBackendServers().size()) {
                    externalServer = holdServer;
                }
            }
            while(externalServer.getFreePort() == 0) {
                Nebula.util.updateFreePort(externalServer);
                Thread.sleep(100);
            }
            int tempPort = externalServer.getFreePort();
            String FinalNewName = newName + "-" + externalServer.getServerName();
            for (BackendServer backendServer : Data.backendInfoMap) {
                if (backendServer.getServerName().equals(FinalNewName)) {
                    Nebula.util.sendMessage(source, Messages.ALREADY_EXISTS.replace("<name>", FinalNewName));
                    return null;
                }
            }
            String command = String.format("docker run -d -e PAPER_VELOCITY_SECRET=%s %s -p %d:25565 --name %s %s", Data.vsecret, Data.envVars, tempPort, FinalNewName, templateName);
            Nebula.util.sendMessage(source, Messages.CREATE_CONTAINER.replace("<name>", FinalNewName));
            BackendServer backendServer = new BackendServer(FinalNewName, externalServer, tempPort, false, source, templateName, starterFlags);
            HoldServer finalExternalServer = externalServer;
            executeSSHCommand(externalServer, command,
                    () -> {
                        ServerInfo newInfo = new ServerInfo(FinalNewName, new InetSocketAddress(finalExternalServer.getIp(), tempPort));
                        server.registerServer(newInfo);
                        Data.backendInfoMap.add(backendServer);
                        Nebula.util.updateFreePort(finalExternalServer);
                        Nebula.util.sendMessage(source, Messages.DONE);
                        backendServer.removeFlag("retry");
                    },
                    () -> {
                        Nebula.util.sendMessage(source, Messages.ERROR_CREATE.replace("<name>", FinalNewName));
                        if (backendServer.getFlags().contains("retry")) {
                            List<String> flags = new ArrayList<>(List.of(starterFlags));
                            flags.remove("retry");
                            createFromTemplate(templateName, newName, source, flags.toArray(new String[0]));
                        }
                    }
            );
            return backendServer;
        } catch (Exception e) {
            return null;
        }
    }

    public void kill(BackendServer serverToKill, CommandSource source) {
        String name = serverToKill.getServerName();
        if(!serverToKill.isOnline()) {
            Nebula.util.sendMessage(source, Messages.SERVER_STOPPED.replace("<name>", name));
            return;
        }
        kickAll(serverToKill);
        Nebula.util.sendMessage(source, Messages.KILL_CONTAINER.replace("<name>", name));
        executeSSHCommand(serverToKill.getHoldServer(), "docker kill " + name,
                () -> Nebula.util.sendMessage(source, Messages.DONE),
                () -> Nebula.util.sendMessage(source, Messages.ERROR_KILL.replace("<name>", name))
        );
    }

    private void kickAll(BackendServer backendServer) {
        server.getServer(backendServer.getServerName()).ifPresent(serverInfo -> {
            for (Player p : serverInfo.getPlayersConnected()) {
                if(backendServer.getFlags().contains("lobby")) {
                    p.disconnect(Component.empty());
                } else {
                    Nebula.util.connectPlayer(p, Nebula.defaultsManager.getTarget(), true);
                }
            }
        });
    }

    public void start(BackendServer serverToStart, CommandSource source) {
        String name = serverToStart.getServerName();
        if(serverToStart.isOnline()) {
            Nebula.util.sendMessage(source, Messages.SERVER_RUNNING.replace("<name>", name));
            return;
        }
        Nebula.util.sendMessage(source, Messages.START_CONTAINER.replace("<name>", name));
        executeSSHCommand(serverToStart.getHoldServer(), "docker start " + name,
                () -> Nebula.util.sendMessage(source, Messages.DONE),
                () -> Nebula.util.sendMessage(source, Messages.ERROR_START.replace("<name>", name))
        );
    }

    public void pull(HoldServer externalServer, String template, CommandSource source) {
        String externName = externalServer.getServerName();
        Nebula.util.sendMessage(source, Messages.PULL_TEMPLATE.replace("<name>", externName).replace("<template>", template));
        executeSSHCommand(externalServer, "docker pull " + template,
                () -> Nebula.util.sendMessage(source, Messages.DONE_PULL.replace("<name>", externName).replace("<template>", template)),
                () -> Nebula.util.sendMessage(source, Messages.ERROR_PULL.replace("<name>", externName).replace("<template>", template))
        );
    }

    public void delete(BackendServer serverToDelete, CommandSource source) {
        kickAll(serverToDelete);
        String name = serverToDelete.getServerName();
        HoldServer externalServer = serverToDelete.getHoldServer();
        Nebula.util.sendMessage(source, Messages.DELETE_CONTAINER.replace("<name>", name));
        executeSSHCommand(externalServer, "docker rm -f " + name,
                () -> {
                    server.unregisterServer(new ServerInfo(name, new InetSocketAddress(externalServer.getIp(), serverToDelete.getPort())));
                    Data.backendInfoMap.remove(serverToDelete);
                    Nebula.util.sendMessage(source, Messages.DONE);
                },
                () -> Nebula.util.sendMessage(source, Messages.ERROR_DELETE.replace("<name>", name))
        );
    }
}
