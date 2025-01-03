package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Container;
import de.voasis.nebula.model.Proxy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

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
        Config.masterProxy = Config.proxyMap.getFirst();
        Config.proxyMap.add(new Proxy("Main", "localhost", Config.multiProxyPort, Config.multiProxyLevel));
        for(Proxy p : Config.proxyMap) {
            Nebula.util.log("Proxy {} has priority of {}.", p.getName(), p.getLevel());
            if(p.getLevel() > Config.masterProxy.getLevel()) {
                Config.masterProxy = p;
            }
        }
        Nebula.util.log("Master proxy: {}.", Config.masterProxy.getName());
    }


    private static void handleClient(Socket socket, String clientIP) {
        clientIP = clientIP.split(":")[0].replace("/", "");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String messageLine;
            while ((messageLine = in.readLine()) != null) {
                String[] parts = messageLine.split("\\|");
                if (parts.length != 2) {
                    out.println("Invalid message format!");
                    Nebula.util.log("Valid = false (Invalid format) | IP: " + clientIP);
                    continue;
                }
                String message = parts[0];
                String receivedHash = parts[1];
                String calculatedHash = Nebula.util.calculateHMAC(message, Config.HMACSecret);
                boolean isValid = calculatedHash.equals(receivedHash);
                if (isValid) {
                    for(Proxy p : Config.proxyMap) {
                        if(p.getIP().equals(clientIP)) {
                            p.setOnline(true);
                        }
                    }
                    processMessage(message, out);
                } else {
                    out.println("failed");
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

    private static void processMessage(String message, PrintWriter out) {
        switch (message) {
            case "online":
                out.println("metoo");
                break;
            case "listservers":
                String all = Config.backendInfoMap.stream()
                        .map(Container::getServerName)
                        .collect(Collectors.joining(","));
                out.println(all);
                Nebula.util.log("[MP-API] Request for listing the servers, returning {}.", all);
                break;
            case "playercount":
                int count = Nebula.server.getPlayerCount();
                out.println(count);
                Nebula.util.log("[MP-API] Request for getting the player count, returning {}.", count);
                break;
        }
    }
}