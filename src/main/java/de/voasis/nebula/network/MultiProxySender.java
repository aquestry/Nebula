package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Proxy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class MultiProxySender {

    public MultiProxySender() {
        pingProxys();
        Nebula.multiProxyServer.refreshMaster();
    }

    public void pingProxys() {
        for(Proxy p : Config.proxyMap) {
            sendMessage(p, "PING", response -> {
                if(!p.isOnline() && !response.equals("FAILED")) {
                    p.setOnline(true);
                    Nebula.multiProxyServer.refreshMaster();
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

    public String getServers(Proxy proxy) {
        AtomicReference<String> result = new AtomicReference<>("FAILED");
        sendMessage(proxy, "GET&SERVERS", result::set, () -> {});
        return result.get();
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