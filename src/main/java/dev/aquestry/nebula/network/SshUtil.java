package dev.aquestry.nebula.network;

import com.jcraft.jsch.*;
import dev.aquestry.nebula.Nebula;
import dev.aquestry.nebula.data.Config;
import dev.aquestry.nebula.model.Node;
import java.io.*;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SshUtil {

    private final Map<Node, Session> sessionPool = new HashMap<>();

    public void init(Node node) {
        try {
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
            Config.nodeMap.add(node);
            Nebula.util.log("Session initialized successfully for node: {}.", node.getServerName());
            updateFreePort(node);
        } catch (JSchException e) {
            Nebula.util.log("Failed to initialize SSH session for node {}.", node.getServerName());
        }
    }

    public void updateFreePort(Node node) {
        if(node.getTag().equals("normal")) {
            ChannelExec channel = null;
            try {
                Session session = sessionPool.get(node);
                if (session == null || !session.isConnected()) {
                    Nebula.util.log("Session not initialized or disconnected. Call init() first for node: {}.", node.getServerName());
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
                    Nebula.util.log("No valid port response from server: {}.", node.getServerName());
                }
            } catch (Exception e) {
                Nebula.util.log("Error updating free port for server {}." + node.getServerName());
            } finally {
                if (channel != null) channel.disconnect();
            }
        } else {
            try {
                ServerSocket socket = new ServerSocket(0);
                node.setFreePort(socket.getLocalPort());
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void executeSSHCommand(Node node, String command, Runnable onSuccess, Runnable onError) {
        if ("normal".equals(node.getTag())) {
            ChannelExec channel = null;
            try {
                Session session = sessionPool.get(node);
                if (session == null || !session.isConnected()) {
                    Nebula.util.log("Session not initialized or disconnected for node: {}.", node.getServerName());
                    onError.run();
                    return;
                }
                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand(command);
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                channel.setOutputStream(responseStream);
                channel.connect(10000);
                while (!channel.isClosed()) Thread.sleep(100);
                String output = responseStream.toString().trim();
                if (!output.isEmpty()) onSuccess.run();
                else {
                    Nebula.util.log("No output from command on server: {}.", node.getServerName());
                    onError.run();
                }
            } catch (Exception e) {
                Nebula.util.log("Error executing SSH command on server {}: {}", node.getServerName(), e.getMessage());
                onError.run();
            } finally {
                if (channel != null) channel.disconnect();
            }
        } else {
            Process process = null;
            try {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb = os.contains("win")
                        ? new ProcessBuilder("cmd.exe", "/c", command)
                        : new ProcessBuilder("sh", "-c", command);
                pb.redirectErrorStream(true);
                process = pb.start();
                StringBuilder outputBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputBuilder.append(line).append("\n");
                    }
                }
                int exitCode = process.waitFor();
                String output = outputBuilder.toString().trim();
                if (!output.isEmpty() && exitCode == 0) onSuccess.run();
                else {
                    Nebula.util.log("Local command error. Exit code: {}. Output: {}", exitCode, output);
                    onError.run();
                }
            } catch (Exception e) {
                Nebula.util.log("Error executing local command: {}", e.getMessage());
                onError.run();
            } finally {
                if (process != null) process.destroy();
            }
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