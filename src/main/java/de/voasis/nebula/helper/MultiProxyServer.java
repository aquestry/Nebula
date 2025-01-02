package de.voasis.nebula.helper;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Container;
import de.voasis.nebula.map.Proxy;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
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
            }
        }).start();
        if(Data.proxyMap.isEmpty()) {
            Nebula.util.log("No proxies found, shutting down.");
            Nebula.server.shutdown();
            return;
        }
        Data.masterProxy = Data.proxyMap.getFirst();
        String externalIP = getExternalIPv4();
        Data.proxyMap.add(new Proxy("THIS", externalIP, Data.multiProxyPort));
        for(Proxy p : Data.proxyMap) {
            Nebula.util.log("Proxy {} has priority of {}.", p.getName(), p.getPriority());
            if(p.getPriority() > Data.masterProxy.getPriority()) {
                Data.masterProxy = p;
            }
        }
        Nebula.util.log("Master proxy: {}.", Data.masterProxy.getName());
    }

    public static String getExternalIPv4() {
        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return in.readLine();
            }
        } catch (Exception e) {
            System.err.println("Error fetching external IP: " + e.getMessage());
            return null;
        }
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
                        if(p.getIP().equals(clientIP) && !p.isOnline()) {
                            p.setOnline(true);
                        }
                    }
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
                    }
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
}