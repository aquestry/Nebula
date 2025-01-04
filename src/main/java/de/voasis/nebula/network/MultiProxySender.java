package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Proxy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiProxySender {
    public MultiProxySender() {
        for(Proxy p : Config.proxyMap) {
            try {
                p.setSocket(new Socket(p.getIP(), p.getPort()));
                if(sendMessage(p.getSocket(), "ALIVE").equals("OK")) {
                    p.setOnline(true);
                }
            } catch (Exception e) {
                Nebula.util.log("Proxy {} is offline at initial check.");
            }
        }
    }

    public static String sendMessage(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String hash = Nebula.util.calculateHMAC(message, Config.HMACSecret);
            out.println(message + "|" + hash);
            return in.readLine();
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
            return "FAILED";
        }
    }
}