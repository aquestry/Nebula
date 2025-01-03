package de.voasis.nebula.helper;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Container;
import de.voasis.nebula.map.Proxy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.stream.Collectors;

public class MultiProxyServer {
    public MultiProxyServer() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Data.multiProxyPort)) {
                Nebula.util.log("MultiProxy API started on port " + Data.multiProxyPort);
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
        if(Data.proxyMap.isEmpty()) {
            Nebula.util.log("No proxies found, shutting down.");
            Nebula.server.shutdown();
            return;
        }
        Data.masterProxy = Data.proxyMap.getFirst();
        Data.proxyMap.add(new Proxy("Main", "localhost", Data.multiProxyPort, Data.multiProxyLevel));
        for(Proxy p : Data.proxyMap) {
            Nebula.util.log("Proxy {} has priority of {}.", p.getName(), p.getLevel());
            if(p.getLevel() > Data.masterProxy.getLevel()) {
                Data.masterProxy = p;
            }
        }
        Nebula.util.log("Master proxy: {}.", Data.masterProxy.getName());
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
                String calculatedHash = Nebula.util.calculateHMAC(message, Data.HMACSecret);
                boolean isValid = calculatedHash.equals(receivedHash);
                if (isValid) {
                    for(Proxy p : Data.proxyMap) {
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
            for(Proxy p : Data.proxyMap) {
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
                String all = Data.backendInfoMap.stream()
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