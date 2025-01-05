package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Proxy;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class MultiProxySender {

    public MultiProxySender() {
        pingProxys();
        fetchPermsFromMaster();
    }

    public void pingProxys() {
        for(Proxy p : Config.proxyMap) {
            sendMessage(p, "GET&LEVEL", response -> {
                if(!response.equals("INVALID")) {
                    int level = Integer.parseInt(response);
                    if(level == Config.THIS_PROXY.getLevel()) {
                        Nebula.util.log("{} has the same level, shutting down!");
                        Nebula.server.shutdown();
                        System.exit(0);
                    }
                    if(!p.isOnline()) {
                        p.setOnline(true);
                        Nebula.multiProxyServer.refreshMaster();
                    }
                    if(p.getLevel() != level) {
                        p.setLevel(level);
                    }
                }
            }, () -> {
                if(p.isOnline()) {
                    p.setOnline(false);
                    Nebula.multiProxyServer.refreshMaster();
                }
            });
        }
    }

    public String getNodes(Proxy proxy) {
        AtomicReference<String> result = new AtomicReference<>("FAILED");
        sendMessage(proxy, "GET&NODES", result::set, () -> {});
        return result.get();
    }

    public String getServers(@NotNull Proxy proxy) {
        AtomicReference<String> result = new AtomicReference<>("FAILED");
        sendMessage(proxy, "GET&SERVERS", result::set, () -> {});
        return result.get();
    }

    public void fetchPermsFromMaster() {
        Proxy proxy = Config.proxyMap.stream()
                .filter(Proxy::isOnline)
                .max(Comparator.comparingInt(Proxy::getLevel))
                .orElse(null);
        if (proxy == null) {
            Nebula.util.log("No online proxies available to fetch permissions from.");
            return;
        }
        Nebula.util.log("Fetching permissions from proxy with highest level: {} (Level: {}).", proxy.getName(), proxy.getLevel());
        sendMessage(proxy, "GET&PERM", response -> {
            if (response == null || response.equals("INVALID") || response.isEmpty()) {
                Nebula.util.log("Invalid permission fetch response from proxy {}", proxy.getName());
                return;
            }
            for (String g : response.split("\\+")) {
                String[] parts = g.split("[\\[\\]]");
                if (parts.length < 2) {
                    Nebula.util.log("Group is empty or has an invalid format: {}", g);
                    continue;
                }
                String groupName = parts[0];
                String[] members = parts[1].split(":");
                Nebula.util.log("Group: {}", groupName);
                if (members.length == 0 || (members.length == 1 && members[0].isEmpty())) {
                    Nebula.util.log("No members found in group '{}'.", groupName);
                    continue;
                }
                for (String m : members) {
                    Nebula.util.log(" - Member: {}", m);
                }
            }
        }, () -> Nebula.util.log("Failed to connect to proxy {} for permission fetch.", proxy.getName()));
    }

    private void sendMessage(Proxy proxy, String message, Consumer<String> onSuccess, Runnable onFailure) {
        try {
            Socket socket = new Socket();
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