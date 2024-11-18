package de.voasis.nebula;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.voasis.nebula.Data.Messages;
import de.voasis.nebula.Maps.BackendServer;
import de.voasis.nebula.Data.Data;
import de.voasis.nebula.Maps.HoldServer;
import net.kyori.adventure.text.Component;

import java.net.InetSocketAddress;

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

    public BackendServer createFromTemplate(String templateName, String newName, CommandSource source, String tag) {
        HoldServer externalServer = Data.holdServerMap.getFirst();
        for(HoldServer holdServer : Data.holdServerMap) {
            if(holdServer.getBackendServers().size() < externalServer.getBackendServers().size()) {
                externalServer = holdServer;
            }
        }
        String FinalNewName = newName + "-" + externalServer.getServerName();
        for (BackendServer backendServer : Data.backendInfoMap) {
            if (backendServer.getServerName().equals(FinalNewName)) {
                Nebula.util.sendMessage(source, Messages.ALREADY_EXISTS.replace("<name>", FinalNewName));
                return null;
            }
        }
        int tempPort = externalServer.getFreePort();
        String command = String.format("docker run -d -e PAPER_VELOCITY_SECRET=%s %s -p %d:25565 --name %s %s", Data.vsecret, Data.envVars, tempPort, FinalNewName, templateName);
        Nebula.util.updateFreePort(externalServer);
        System.out.println(command);
        Nebula.util.sendMessage(source, Messages.CREATE_CONTAINER.replace("<name>", FinalNewName));
        BackendServer backendServer = new BackendServer(FinalNewName, externalServer, tempPort, false, source, templateName, tag);
        HoldServer finalExternalServer = externalServer;
        executeSSHCommand(externalServer, command,
                () -> {
                    ServerInfo newInfo = new ServerInfo(FinalNewName, new InetSocketAddress(finalExternalServer.getIp(), tempPort));
                    server.registerServer(newInfo);
                    Data.backendInfoMap.add(backendServer);
                    Nebula.util.sendMessage(source, Messages.DONE);
                },
                () -> Nebula.util.sendMessage(source, Messages.ERROR_CREATE.replace("<name>", FinalNewName))
        );
        return backendServer;
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
    public void kickAll(BackendServer backendServer) {
        server.getServer(backendServer.getServerName()).ifPresent(serverInfo -> {
            for (Player p : serverInfo.getPlayersConnected()) {
                p.disconnect(Component.empty());
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
                () -> Nebula.util.sendMessage(source, Messages.DONE),
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
