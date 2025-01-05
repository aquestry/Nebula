package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Container;
import de.voasis.nebula.model.Node;
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
        if (Config.proxyMap.stream().noneMatch(proxy -> proxy.getIP().equals(clientIP.split(":")[0].replace("/", "")))) {
            Nebula.util.log("Got message from unknown IP: {}.", clientIP);
            return;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String message = in.readLine();
            String[] parts = message.split("\\|");
            if (parts.length != 2 || !Nebula.util.calculateHMAC(parts[0]).equals(parts[1])) {
                out.println("FAILED");
                return;
            }
            out.println(handleGET(parts[0].split("&")));
        } catch (Exception ignored) {}
    }

    private String handleGET(String[] components) {
        switch (components[1]) {
            case "SERVERS": return String.join(",", Config.backendInfoMap.stream().map(Container::getServerName).toList());
            case "NODES": return String.join(",", Config.nodeMap.stream().map(Node::getServerName).toList());
            case "PERM": return String.join("+", Nebula.permissionFile.getGroupNames().stream()
                    .map(g -> g + "[" + String.join(":", Nebula.permissionFile.getGroupMembers(g)) + "]").toList());
            case "LEVEL": return String.valueOf(Config.multiProxyLevel);
            default: return "INVALID";
        }
    }
}
