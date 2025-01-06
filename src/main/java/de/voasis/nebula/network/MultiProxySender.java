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
import java.util.concurrent.atomic.AtomicReference;
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

    public String getNodes(Proxy proxy) {
        AtomicReference<String> result = new AtomicReference<>("FAILED");
        sendMessage(proxy, "GET&NODES", result::set, e -> {});
        return result.get();
    }

    public String getServers(Proxy proxy) {
        AtomicReference<String> result = new AtomicReference<>("FAILED");
        sendMessage(proxy, "GET&SERVERS", result::set, e -> {});
        return result.get();
    }

    public void sendGroups(Proxy proxy) {
        sendMessage(proxy
                , "POST&PERM&" + Nebula.permissionManager.getAllGroups(), response -> {}
                , e -> Nebula.util.log("Failed to connect to proxy {} for permission post."
                , proxy.getName()));
    }

    public void sendGroup(Proxy proxy, Group group) {
        sendMessage(proxy
                , "POST&PERM&" + Nebula.permissionManager.getGroupData(group), response -> {}
                , e -> Nebula.util.log("Failed to connect to proxy {} for permission post."
                        , proxy.getName()));
    }

    private void sendMessage(Proxy proxy, String message, Consumer<String> onSuccess, Consumer<String> onFailure) {
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
        int myLevel = Config.multiProxyLevel;
        return Config.proxyMap.stream()
                .filter(Proxy::isOnline)
                .allMatch(proxy -> proxy.getLevel() < myLevel);
    }
}