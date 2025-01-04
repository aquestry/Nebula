package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Proxy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MultiProxySender {
    public void pingProxys() {
        for(Proxy p : Config.proxyMap) {
            sendMessage(p, "PING", response -> {
                if(!p.isOnline()) {
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

    private void sendMessage(Proxy proxy, String message, java.util.function.Consumer<String> onSuccess, Runnable onFailure) {
        final int TIMEOUT_MS = 2000;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(proxy.getIP(), proxy.getPort()), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                message = message + "|" + Nebula.util.calculateHMAC(message);
                out.println(message);
                onSuccess.accept(in.readLine());
            }
        } catch (Exception e) {
            onFailure.run();
        }
    }
}