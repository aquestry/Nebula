package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Proxy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

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
            } catch (IOException e) {
                Nebula.util.log("Error starting MultiProxy API: " + e.getMessage());
                Nebula.server.shutdown();
            }
        }).start();
        if(Config.proxyMap.isEmpty()) {
            Nebula.util.log("No proxies found, shutting down.");
            Nebula.server.shutdown();
            return;
        }
        Config.proxyMap.add(new Proxy("thisone", "", 0, Config.multiProxyLevel, true));
        recheckMaster();
        Nebula.util.log("Start complete, Master proxy: {}.", Config.masterProxy.getName());
    }

    private void handleClient(Socket socket, String clientIP) {
        clientIP = clientIP.split(":")[0].replace("/", "");
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
                String calculatedHash = Nebula.util.calculateHMAC(message, Config.HMACSecret);
                boolean isValid = calculatedHash.equals(receivedHash);
                if (isValid) {
                    Proxy sender = null;
                    for(Proxy p : Config.proxyMap) {
                        if(p.getIP().equals(clientIP)) {
                            sender = p;
                        }
                    }
                    if(sender == null) return;
                    if(!sender.isOnline()) {
                        sender.setOnline(true);
                        recheckMaster();
                    }
                    if(message.equals("ALIVE")) {
                            out.println("OK");
                            Nebula.util.log("Got alive message from {}.", sender.getName());
                    }
                } else {
                    out.println("FAILED");
                }
            }
        } catch (IOException e) {
            for(Proxy p : Config.proxyMap) {
                if(p.getIP().equals(clientIP)) {
                    p.setOnline(false);
                }
            }
        }
    }

    public void recheckMaster() {
        List<Proxy> proxyList = Config.proxyMap.stream().filter(Proxy::isOnline).toList();
        if(Config.masterProxy == null) {
            Config.masterProxy = proxyList.getFirst();
        }
        Proxy original = Config.masterProxy;
        for(Proxy p : proxyList) {
            if(p.getLevel() > Config.masterProxy.getLevel()) {
                Config.masterProxy = p;
            }
        }
        if(!original.equals(Config.masterProxy)) {
            Nebula.util.log("The new master proxy is: {}.", Config.masterProxy.getName());
        }
    }
}