package de.voasis.nebula.helper;

import com.jcraft.jsch.*;
import de.voasis.nebula.map.HoldServer;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SSH {
    private final Map<HoldServer, Session> sessionPool = new HashMap<>();
    public void init(HoldServer externalServer) {
        try {
            if (sessionPool.containsKey(externalServer) && sessionPool.get(externalServer).isConnected()) {
                System.out.println("Session already initialized and connected for server: " + externalServer.getIp());
                return;
            }
            JSch jsch = new JSch();
            if (externalServer.getPrivateKeyFile() != null && !externalServer.getPrivateKeyFile().equals("none")) {
                jsch.addIdentity(externalServer.getPrivateKeyFile());
            }
            Session session = jsch.getSession(
                    externalServer.getUsername(),
                    externalServer.getIp(),
                    externalServer.getPort()
            );
            if (externalServer.getPassword() != null && !externalServer.getPassword().equals("none")) {
                session.setPassword(externalServer.getPassword());
            }
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(10000);
            sessionPool.put(externalServer, session);
            System.out.println("SSH session initialized successfully for server: " + externalServer.getIp());
            updateFreePort(externalServer);
        } catch (JSchException e) {
            System.err.println("Failed to initialize SSH session for server " + externalServer.getIp() + ": " + e.getMessage());
        }
    }
    public void updateFreePort(HoldServer externalServer) {
        ChannelExec channel = null;
        try {
            Session session = sessionPool.get(externalServer);
            if (session == null || !session.isConnected()) {
                System.err.println("Session not initialized or disconnected. Call init() first for server: " + externalServer.getIp());
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
                externalServer.setFreePort(Integer.parseInt(output));
            } else {
                System.err.println("No valid port response from server: " + externalServer.getIp());
            }
        } catch (Exception e) {
            System.err.println("Error updating free port for server " + externalServer.getIp() + ": " + e.getMessage());
        } finally {
            if (channel != null) channel.disconnect();
        }
    }
    public void executeSSHCommand(HoldServer externalServer, String command, Runnable onSuccess, Runnable onError) {
        ChannelExec channel = null;
        try {
            Session session = sessionPool.get(externalServer);
            if (session == null || !session.isConnected()) {
                System.err.println("Session not initialized or disconnected. Call init() first for server: " + externalServer.getIp());
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
                System.err.println("No output from command on server: " + externalServer.getIp());
                onError.run();
            }
        } catch (Exception e) {
            System.err.println("Error executing SSH command on server " + externalServer.getIp() + ": " + e.getMessage());
            onError.run();
        } finally {
            if (channel != null) channel.disconnect();
        }
    }
    public void closeAll() {
        for (Map.Entry<HoldServer, Session> entry : sessionPool.entrySet()) {
            if (entry.getValue().isConnected()) {
                entry.getValue().disconnect();
            }
        }
        sessionPool.clear();
    }
}