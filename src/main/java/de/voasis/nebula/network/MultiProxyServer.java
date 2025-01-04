package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Proxy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiProxyServer {
    public MultiProxyServer() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Config.multiProxyPort)) {
                Nebula.util.log("MultiProxy API started on port " + Config.multiProxyPort);
                while (true) {
                    Socket socket = server.accept();
                    String clientIP = socket.getRemoteSocketAddress().toString();
                    new Thread(() -> handleClient(socket, clientIP)).start();
                }
            } catch (Exception e) {
                Nebula.util.log("Error starting MultiProxy API: " + e.getMessage());
                Nebula.server.shutdown();
            }
        }).start();
        if(Config.proxyMap.isEmpty()) {
            Nebula.util.log("No proxies found, shutting down.");
            Nebula.server.shutdown();
        }
    }

    private void handleClient(Socket socket, String clientIP) {
        if(!Config.proxyMap.stream().anyMatch(proxy -> proxy.getIP().equals(clientIP.split(":")[0].replace("/", "")))) {
            Nebula.util.log("Got message from unknown IP: {}.", clientIP);
            return;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String messageLine;
            while ((messageLine = in.readLine()) != null) {
                String[] parts = messageLine.split("\\|");
                if (parts.length != 2) {
                    out.println("FAILED");
                    continue;
                }
                String message = parts[0];
                String receivedHash = parts[1];
                String calculatedHash = Nebula.util.calculateHMAC(message);
                boolean isValid = calculatedHash.equals(receivedHash);
                if (isValid) {
                    out.println("SUCCESS");
                    // Give better response
                } else {
                    out.println("FAILED");
                }
            }
        } catch (IOException ignored) {}
    }

    public void refreshMaster() {
        if(Config.masterProxy == null || !Config.masterProxy.isOnline()) {
            Config.masterProxy = Config.THIS_PROXY;
        }
        for(Proxy p : Config.proxyMap.stream().filter(Proxy::isOnline).toList()) {
            if(p.getLevel() > Config.masterProxy.getLevel()) {
                Config.masterProxy = p;
            }
        }
        Nebula.util.log("Master proxy: {}.", Config.masterProxy.getName());
    }
}