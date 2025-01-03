package de.voasis.nebula.manager;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Messages;
import de.voasis.nebula.model.Container;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Queue;
import de.voasis.nebula.model.Node;
import net.kyori.adventure.text.Component;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ContainerManager {
    public Container createFromTemplate(String templateName, String newName, CommandSource source, String... starterFlags) {
        try {
            Node node = Config.nodeMap.getFirst();
            for(Node n : Config.nodeMap) {
                if(n.getBackendServers().size() < node.getBackendServers().size()) {
                    node = n;
                }
            }
            while(node.getFreePort() == 0) {
                Nebula.ssh.updateFreePort(node);
                Thread.sleep(100);
            }
            int tempPort = node.getFreePort();
            String FinalNewName = newName + "-" + node.getServerName();
            for (Container container : Config.backendInfoMap) {
                if (container.getServerName().equals(FinalNewName)) {
                    Nebula.util.sendMessage(source, Messages.ALREADY_EXISTS.replace("<name>", FinalNewName));
                    return null;
                }
            }
            StringBuilder envVars = new StringBuilder(Config.envVars);
            for(String flags : starterFlags) {
                for(Queue queue : Config.queueMap) {
                    if(queue.getName().equals(flags.replace("gamemode:", ""))) {
                        envVars.append(queue.getLocalEnvVars());
                        break;
                    }
                }
            }
            String command = String.format("docker run -d %s -p %d:25565 --name %s %s", envVars, tempPort, FinalNewName, templateName);
            Nebula.util.sendMessage(source, Messages.CREATE_CONTAINER.replace("<name>", FinalNewName));
            Container container = new Container(FinalNewName, node, tempPort, false, source, templateName, starterFlags);
            Node finalnode = node;
            Nebula.ssh.executeSSHCommand(node, command,
                    () -> {
                        ServerInfo newInfo = new ServerInfo(FinalNewName, new InetSocketAddress(finalnode.getIp(), tempPort));
                        Nebula.server.registerServer(newInfo);
                        Config.backendInfoMap.add(container);
                        Nebula.ssh.updateFreePort(finalnode);
                        Nebula.util.sendMessage(source, Messages.DONE);
                        container.removeFlag("retry");
                    },
                    () -> {
                        Nebula.util.sendMessage(source, Messages.ERROR_CREATE.replace("<name>", FinalNewName));
                        if (container.getFlags().contains("retry")) {
                            List<String> flags = new ArrayList<>(List.of(starterFlags));
                            flags.remove("retry");
                            createFromTemplate(templateName, newName, source, flags.toArray(new String[0]));
                        }
                    }
            );
            return container;
        } catch (Exception e) {
            return null;
        }
    }

    public void kill(Container serverToKill, CommandSource source) {
        String name = serverToKill.getServerName();
        if(!serverToKill.isOnline()) {
            Nebula.util.sendMessage(source, Messages.SERVER_STOPPED.replace("<name>", name));
            return;
        }
        kickAll(serverToKill);
        Nebula.util.sendMessage(source, Messages.KILL_CONTAINER.replace("<name>", name));
        Nebula.ssh.executeSSHCommand(serverToKill.getHoldServer(), "docker kill " + name,
                () -> Nebula.util.sendMessage(source, Messages.DONE),
                () -> Nebula.util.sendMessage(source, Messages.ERROR_KILL.replace("<name>", name))
        );
    }

    private void kickAll(Container container) {
        Nebula.server.getServer(container.getServerName()).ifPresent(serverInfo -> {
            for (Player p : serverInfo.getPlayersConnected()) {
                if(container.getFlags().contains("lobby")) {
                    p.disconnect(Component.empty());
                } else {
                    Nebula.util.connectPlayer(p, Nebula.defaultsManager.getTarget(), true);
                }
            }
        });
    }

    public void start(Container serverToStart, CommandSource source) {
        String name = serverToStart.getServerName();
        if(serverToStart.isOnline()) {
            Nebula.util.sendMessage(source, Messages.SERVER_RUNNING.replace("<name>", name));
            return;
        }
        Nebula.util.sendMessage(source, Messages.START_CONTAINER.replace("<name>", name));
        Nebula.ssh.executeSSHCommand(serverToStart.getHoldServer(), "docker start " + name,
                () -> Nebula.util.sendMessage(source, Messages.DONE),
                () -> Nebula.util.sendMessage(source, Messages.ERROR_START.replace("<name>", name))
        );
    }

    public void pull(Node node, String template, CommandSource source) {
        String nodeName = node.getServerName();
        Nebula.util.sendMessage(source, Messages.PULL_TEMPLATE.replace("<name>", nodeName).replace("<template>", template));
        Nebula.ssh.executeSSHCommand(node, "docker pull " + template,
                () -> Nebula.util.sendMessage(source, Messages.DONE_PULL.replace("<name>", nodeName).replace("<template>", template)),
                () -> Nebula.util.sendMessage(source, Messages.ERROR_PULL.replace("<name>", nodeName).replace("<template>", template))
        );
    }

    public void delete(Container serverToDelete, CommandSource source) {
        kickAll(serverToDelete);
        String name = serverToDelete.getServerName();
        Node node = serverToDelete.getHoldServer();
        Nebula.util.sendMessage(source, Messages.DELETE_CONTAINER.replace("<name>", name));
        Nebula.ssh.executeSSHCommand(node, "docker rm -f " + name,
                () -> {
                    Nebula.server.unregisterServer(new ServerInfo(name, new InetSocketAddress(node.getIp(), serverToDelete.getPort())));
                    Config.backendInfoMap.remove(serverToDelete);
                    Nebula.util.sendMessage(source, Messages.DONE);
                },
                () -> Nebula.util.sendMessage(source, Messages.ERROR_DELETE.replace("<name>", name))
        );
    }
}