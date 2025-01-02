package de.voasis.nebula.helper;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Proxy;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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
                // Nebula.util.log("Valid = " + isValid + " | Message: " + message + " | ReceivedHash: " + receivedHash + " | CalculatedHash: " + calculatedHash + " | IP: '" + clientIP + "'");
                if (isValid) {
                    switch (message) {
                        case "online":
                            for(Proxy p : Data.proxyMap) {
                                if(p.getIP().equals(clientIP)) {
                                    p.setOnline(true);
                                }
                            }
                            out.println("metoo");
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