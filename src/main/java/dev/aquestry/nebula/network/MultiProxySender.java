package dev.aquestry.nebula.network;

import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.model.Group;
import dev.aquestry.nebula.model.Proxy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

public class MultiProxySender {

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
                    if (proxy.getLevel() != level) {
                        proxy.setLevel(level);
                    }
                    if (!proxy.isOnline()) {
                        proxy.setOnline(true);
                        if(hasHighestLevel()) {
                            sendGroups(proxy);
                        }
                    }
                }}, e -> {
                if (proxy.isOnline()) {
                    proxy.setOnline(false);
                }
            });
        }
    }

    private void sendGroups(Proxy proxy) {
        sendMessage(proxy
                , "POST&PERM&UPDATE&" + Nebula.permissionManager.getAllGroups(), response -> {}
                , e -> Nebula.util.log("Failed to connect to proxy {} for group post."
                , proxy.getName()));
    }

    public String getNodes(Proxy proxy) {
        final String[] result = {"FAILED"};
        sendMessage(proxy, "GET&NODES",
                response -> result[0] = response, e -> {});
        return result[0];
    }

    public String getServers(Proxy proxy) {
        final String[] result = {"FAILED"};
        sendMessage(proxy, "GET&SERVERS",
                response -> result[0] = response, e -> {});
        return result[0];
    }

    public void updateGroup(Group group) {
        for(Proxy proxy : Config.proxyMap.stream().filter(Proxy::isOnline).toList()) {
            sendMessage(proxy
                    , "POST&PERM&UPDATE&" + Nebula.permissionManager.getGroupData(group), response -> {}
                    , e -> Nebula.util.log("Failed to connect to proxy {} for group post."
                            , proxy.getName()));
        }
    }

    public void sendDelete(String groupName) {
        for(Proxy proxy : Config.proxyMap.stream().filter(Proxy::isOnline).toList()) {
            sendMessage(proxy
                    , "POST&PERM&DELETE&" + groupName, response -> {}
                    , e -> Nebula.util.log("Failed to connect to proxy {} for group deletion."
                            , proxy.getName()));
        }
    }

    private void sendMessage(Proxy proxy, String message, Consumer<String> onSuccess, Consumer<String> onFailure) {
        if(Config.quitting) return;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(proxy.getIP(), proxy.getPort()), 2000);
            socket.setSoTimeout(2000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            message = message + "|" + Nebula.util.calculateHMAC(message);
            out.println(message);
            onSuccess.accept(in.readLine());
        } catch (Exception e) {
            onFailure.accept(e.getMessage());
        }
    }

    public boolean hasHighestLevel() {
        return Config.proxyMap.stream()
                .filter(Proxy::isOnline)
                .allMatch(proxy -> proxy.getLevel() < Config.multiProxyLevel);
    }
}