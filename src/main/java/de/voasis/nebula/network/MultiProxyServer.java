package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Proxy;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiProxyServer {
    public MultiProxyServer() {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Config.multiProxyPort)) {
                Nebula.util.log("MultiProxy API started on port {}.", Config.multiProxyPort);
                while (true) {
                    Socket socket = server.accept();
                    String clientIP = socket.getRemoteSocketAddress().toString();
                    new Thread(() -> handleClient(socket, clientIP)).start();
                }
            } catch (Exception e) {
                Nebula.util.log("Error starting MultiProxy API: {}.", e.getMessage());
                Nebula.server.shutdown();
            }
        }).start();
    }

    private void handleClient(Socket socket, String clientIP) {
        String ip = clientIP.split(":")[0].replace("/", "");
        if (Config.proxyMap.stream().noneMatch(proxy -> proxy.getIP().equals(ip))) {
            Nebula.util.log("Got message from unknown IP: {}.", clientIP);
            return;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String message = in.readLine();
            String[] parts = message.split("\\|");
            Proxy proxy = Config.proxyMap.stream().filter(p -> p.getIP().equals(ip)).findFirst().orElse(null);
            if (parts.length != 2 || !Nebula.util.calculateHMAC(parts[0]).equals(parts[1]) || proxy == null) {
                out.println("FAILED");
                return;
            }
            String[] components = parts[0].split("&");
            if(components.length == 4) {
                if(components[0].equals("POST")) {
                    out.println(handlePOST(components));
                }
            }
            if(components.length == 2) {
                if(components[0].equals("GET")) {
                    out.println(handleGET(components));
                }

            }
        } catch (Exception ignored) {}
    }

    private String handleGET(String[] components) {
        switch (components[1]) {
            case "LEVEL": return String.valueOf(Config.multiProxyLevel);
            default: return "INVALID";
        }
    }

    private String handlePOST(String[] components) {
        switch (components[2]) {
            case "DELETE":
                Nebula.permissionFile.deleteGroup(components[3]);
                return "FETCHED";
            case "UPDATE":
                Nebula.permissionManager.processGroups(components[3]);
                return "FETCHED";
            default: return "INVALID";
        }
    }
}