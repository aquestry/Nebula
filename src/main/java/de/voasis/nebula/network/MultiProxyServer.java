package de.voasis.nebula.network;

import de.voasis.nebula.Nebula;
import de.voasis.nebula.data.Config;
import de.voasis.nebula.model.Container;
import de.voasis.nebula.model.Group;
import de.voasis.nebula.model.Node;
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
            if (parts.length != 2 || !Nebula.util.calculateHMAC(parts[0]).equals(parts[1])) {
                out.println("FAILED");
                return;
            }
            String[] components = parts[0].split("&");
            if(components[0].equals("POST") && components[1].equals("PERM")) {
                Proxy proxy = Config.proxyMap.stream().filter(p -> p.getIP().equals(ip)).findFirst().orElse(null);
                if(proxy != null) {
                    Nebula.util.log("Recieved permissions.");
                    processGroups(components[2]);
                    out.println("FETCHED");
                    return;
                }
            }
            if(components[0].equals("GET")) {
                out.println(handleGET(components));
            }
        } catch (Exception ignored) {}
    }

    private String handleGET(String[] components) {
        switch (components[1]) {
            case "SERVERS": return String.join(",", Config.backendInfoMap.stream().map(Container::getServerName).toList());
            case "NODES": return String.join(",", Config.nodeMap.stream().map(Node::getServerName).toList());
            case "PERM": return Nebula.permissionManager.getAllGroups();
            case "LEVEL": return String.valueOf(Config.multiProxyLevel);
            default: return "INVALID";
        }
    }
    
    public void processGroups(String response) {
        int updated = 0;
        for (String groupData : response.split("~")) {
            try {
                groupData = groupData.trim();
                String[] parts = groupData.split("\\?");
                String groupName = parts[0].trim();
                String prefix = parts[1].replace("<space>", " ");
                int level = Integer.parseInt(parts[2].trim());
                String[] members = new String[0];
                String[] perms = new String[0];
                if(parts.length == 4) {
                    members = parts[3].split("°")[0].replace("[","").replace("]","").split(":");
                    perms = parts[3].split("°")[1].split(":");
                }
                Group group = Nebula.permissionFile.createGroup(groupName, prefix, level);
                Nebula.util.log("Processed group '{}' with level {} and prefix '{}'.", groupName, level, prefix);
                Nebula.permissionFile.clearMembers(group);
                for (String member : members) {
                    if (!member.isEmpty()) {
                        Nebula.permissionManager.assignGroup(member, group);
                        Nebula.util.log("Added member '{}' to group '{}'.", member, groupName);
                    }
                }
                Nebula.permissionFile.clearPermissions(group);
                for (String perm : perms) {
                    if (!perm.isEmpty()) {
                        Nebula.permissionFile.addPermissionToGroup(group, perm);
                        Nebula.util.log("Added permission '{}' to group '{}'.", perm, groupName);
                    }
                }
                updated++;
            } catch (NumberFormatException e) {
                Nebula.util.log("Failed to parse level in group data '{}'. Error: {}", groupData, e.getMessage());
            } catch (Exception e) {
                Nebula.util.log("Failed to process group data '{}'. Error: {}", groupData, e.getMessage());
            }
        }
        Nebula.permissionFile.saveConfig();
        Nebula.permissionFile.sendAlltoBackend();
        Nebula.util.log("Group processing completed. Total groups updated: {}.", updated);
    }
}