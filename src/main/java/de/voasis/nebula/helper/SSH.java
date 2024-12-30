package de.voasis.nebula.helper;

import com.jcraft.jsch.*;
import de.voasis.nebula.map.Node;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SSH {

    private final Map<Node, Session> sessionPool = new HashMap<>();

    public void init(Node node) {
        try {
            if (sessionPool.containsKey(node) && sessionPool.get(node).isConnected()) {
                System.out.println("Session already initialized and connected for server: " + node.getIp());
                return;
            }
            JSch jsch = new JSch();
            if (node.getPrivateKeyFile() != null && !node.getPrivateKeyFile().equals("none")) {
                jsch.addIdentity(node.getPrivateKeyFile());
            }
            Session session = jsch.getSession(node.getUsername(), node.getIp(), node.getPort());
            if (node.getPassword() != null && !node.getPassword().equals("none")) {
                session.setPassword(node.getPassword());
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(10000);
            sessionPool.put(node, session);
            System.out.println("SSH session initialized successfully for server: " + node.getIp());
            node.setActive(true);
            updateFreePort(node);
        } catch (JSchException e) {
            System.err.println("Failed to initialize SSH session for server " + node.getIp() + ": " + e.getMessage());
            node.setActive(false);
        }
    }

    public void updateFreePort(Node node) {
        ChannelExec channel = null;
        try {
            Session session = sessionPool.get(node);
            if (session == null || !session.isConnected()) {
                System.err.println("Session not initialized or disconnected. Call init() first for server: " + node.getIp());
                return;
            }
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("ruby -e 'require \"socket\"; puts Addrinfo.tcp(\"\", 0).bind {|s| s.local_address.ip_port }'");
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect(10000);
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }
            String output = responseStream.toString().trim();
            if (!output.isEmpty()) {
                node.setFreePort(Integer.parseInt(output));
            } else {
                System.err.println("No valid port response from server: " + node.getIp());
            }
        } catch (Exception e) {
            System.err.println("Error updating free port for server " + node.getIp() + ": " + e.getMessage());
        } finally {
            if (channel != null) channel.disconnect();
        }
    }

    public void executeSSHCommand(Node node, String command, Runnable onSuccess, Runnable onError) {
        ChannelExec channel = null;
        try {
            Session session = sessionPool.get(node);
            if (session == null || !session.isConnected()) {
                System.err.println("Session not initialized or disconnected. Call init() first for server: " + node.getIp());
                return;
            }
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect(10000);
            while (!channel.isClosed()) {
                Thread.sleep(100);
            }
            String output = responseStream.toString().trim();
            if (!output.isEmpty()) {
                onSuccess.run();
            } else {
                System.err.println("No output from command on server: " + node.getIp());
                onError.run();
            }
        } catch (Exception e) {
            System.err.println("Error executing SSH command on server " + node.getIp() + ": " + e.getMessage());
            onError.run();
        } finally {
            if (channel != null) channel.disconnect();
        }
    }

    public void closeAll() {
        for (Map.Entry<Node, Session> entry : sessionPool.entrySet()) {
            if (entry.getValue().isConnected()) {
                entry.getValue().disconnect();
            }
        }
        sessionPool.clear();
    }
}