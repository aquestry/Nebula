package de.voasis.nebula.helper;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Data;
import de.voasis.nebula.map.Proxy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiProxyClient {
    public MultiProxyClient() {
        for(Proxy p : Data.proxyMap) {
            try {
                p.setSocket(new Socket(p.getIP(), p.getPort()));
                if(sendMessage(p.getSocket(), "online").equals("metoo")) {
                    p.setOnline(true);
                }
            } catch (Exception e) {
                Nebula.util.log("Proxy: " + p.getName() + " is offline");
            }
        }
    }

    public static String sendMessage(Socket socket, String message) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String hash = Nebula.util.calculateHMAC(message, Data.HMACSecret);
            out.println(message + "|" + hash);
            return in.readLine();
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
            return null;
        }
    }
}