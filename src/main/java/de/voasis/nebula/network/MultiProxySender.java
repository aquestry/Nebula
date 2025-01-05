package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Group;
import de.voasis.nebula.model.Proxy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class MultiProxySender {

    public MultiProxySender() {
        pingProxies();
        Proxy proxy = Config.proxyMap.stream()
                .filter(Proxy::isOnline)
                .max(Comparator.comparingInt(Proxy::getLevel))
                .orElse(null);
        fetchPermissions(proxy);
    }

    public void pingProxies() {
        for (Proxy proxy : Config.proxyMap) {
            sendMessage(proxy, "GET&LEVEL", response -> {
                if (!response.equals("INVALID")) {
                    int level = Integer.parseInt(response);
                    if (level == Config.multiProxyLevel) {
                        Nebula.util.log("{} has the same level, shutting down!", proxy.getName());
                        Nebula.server.shutdown();
                        System.exit(0);
                    }
                    if (!proxy.isOnline()) {
                        proxy.setOnline(true);
                    }
                    if (proxy.getLevel() != level) {
                        proxy.setLevel(level);
                    }
                }
            }, () -> {
                if (proxy.isOnline()) {
                    proxy.setOnline(false);
                }
            });
        }
    }

    public String getNodes(Proxy proxy) {
        AtomicReference<String> result = new AtomicReference<>("FAILED");
        sendMessage(proxy, "GET&NODES", result::set, () -> {});
        return result.get();
    }

    public String getServers(Proxy proxy) {
        AtomicReference<String> result = new AtomicReference<>("FAILED");
        sendMessage(proxy, "GET&SERVERS", result::set, () -> {});
        return result.get();
    }

    public void fetchPermissions(Proxy proxy) {
        if (proxy == null) {
            Nebula.util.log("No online proxies available to fetch permissions from.");
            return;
        }
        sendMessage(proxy, "GET&PERM", response -> {
            if (response == null || response.equals("INVALID") || response.isEmpty()) {
                return;
            }
            processFetchedPermissions(response);
        }, () -> Nebula.util.log("Failed to connect to proxy {} for permission fetch.", proxy.getName()));
    }

    public void sendGroups() {
        for(Proxy proxy : Config.proxyMap.stream().filter(Proxy::isOnline).toList()) {
            sendMessage(proxy
                    , "POST&PERM", response -> {}
                    , () -> Nebula.util.log("Failed to connect to proxy {} for permission post."
                    , proxy.getName()));
        }
    }

    private void processFetchedPermissions(String response) {
        List<Group> groupsMentioned = new ArrayList<>();
        for (String groupData : response.split("\\+")) {
            String[] parts = groupData.split("[\\[\\]]");
            if (parts.length < 2) {
                continue;
            }
            String groupName = parts[0].split("\\?")[0];
            String prefix = parts[0].split("\\?")[1];
            int level = Integer.parseInt(parts[0].split("\\?")[2]);
            String[] members = parts[1].split(":");
            Group group = Nebula.permissionFile.createGroup(groupName, prefix, level);
            groupsMentioned.add(group);
            Nebula.permissionFile.clearMembers(group);
            for (String member : members) {
                Nebula.permissionManager.assignGroup(member, group);
            }
        }
        for(Group group : Nebula.permissionFile.runtimeGroups) {
            if(!groupsMentioned.contains(group)) {
                Nebula.permissionFile.deleteGroup(group.getName());
            }
        }
        Nebula.permissionFile.saveConfig();
        Nebula.permissionFile.sendAlltoBackend();
    }

    private void sendMessage(Proxy proxy, String message, Consumer<String> onSuccess, Runnable onFailure) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(proxy.getIP(), proxy.getPort()), 2000);
            socket.setSoTimeout(2000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            message = message + "|" + Nebula.util.calculateHMAC(message);
            out.println(message);
            onSuccess.accept(in.readLine());
        } catch (Exception e) {
            onFailure.run();
        }
    }
}
