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
        Proxy proxy = Config.proxyMap.stream().filter(p -> p.getIP().equals(ip)).findFirst().orElse(null);
        if (proxy == null) return;
        try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var out = new PrintWriter(socket.getOutputStream(), true)) {
            String[] parts = in.readLine().split("\\|");
            if (parts.length != 2 || !Nebula.util.calculateHMAC(parts[0]).equals(parts[1])) {
                out.println("FAILED"); return;
            }
            String[] c = parts[0].split("&");
            if(c[0].equals("POST")) {
                if(c[1].equals("PERM") && c.length == 4) {
                    out.println(handlePERM(c));
                }
            }
            if(c[0].equals("GET")) {
                out.println(handleGET(c));
            }
            out.println("INVALID");
        } catch (Exception ignored) {}
    }

    private String handleGET(String[] components) {
        switch (components[1]) {
            case "LEVEL": return String.valueOf(Config.multiProxyLevel);
            case "NODES": return String.join(",", Config.nodeMap.stream().map(Node::getServerName).toList());
            case "SERVERS": return String.join(",", Config.containerMap.stream().map(Container::getServerName).toList());
            default: return "INVALID";
        }
    }

    private String handlePERM(String[] components) {
        switch (components[2]) {
            case "DELETE": Nebula.permissionFile.deleteGroup(components[3]); return "FETCHED";
            case "UPDATE": processGroups(components[3]); return "FETCHED";
            default: return "INVALID";
        }
    }

    public void processGroups(String response) {
        int updated = 0;
        for (String groupData : response.split("~")) {
            try {
                String[] parts = groupData.split("\\?");
                String groupName = parts[0].trim();
                String prefix = parts[1].replace("<space>", " ");
                int level = Integer.parseInt(parts[2].trim());
                String[] membersRaw = new String[0];
                String[] perms = new String[0];
                if (parts.length == 4) {
                    String[] uuidsPerm = parts[3].split("°");
                    if (uuidsPerm.length > 0) {
                        membersRaw = uuidsPerm[0].replace("[", "").replace("]", "").split(",");
                    }
                    if (uuidsPerm.length == 2) {
                        perms = uuidsPerm[1].split(",");
                    }
                }
                String [] members = new String[membersRaw.length];
                for (int i = 0; i < membersRaw.length; i++)
                    members[i] = membersRaw[i].trim();
                Group group = Nebula.permissionFile.createGroup(groupName, prefix, level);
                Nebula.permissionFile.clearMembers(group);
                for (String member : members) {
                    if (!member.isEmpty()) {
                        Nebula.permissionManager.assignGroup(member, group);
                    }
                }
                Nebula.permissionFile.clearPermissions(group);
                for (String perm : perms) {
                    if (!perm.isEmpty()) {
                        Nebula.permissionFile.addPermissionToGroup(group, perm);
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
        Nebula.util.log("Group syncing completed. Total groups updated: {}.", updated);
    }
}