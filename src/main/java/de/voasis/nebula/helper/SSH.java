package de.voasis.nebula.helper;

import com.jcraft.jsch.*;
import de.voasis.nebula.map.HoldServer;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class SSH {
    public void updateFreePort(HoldServer externalServer) {
        Session session = null;
        ChannelExec channel = null;
        try {
            JSch jsch = new JSch();
            if (externalServer.getPrivateKeyFile() != null && !externalServer.getPrivateKeyFile().equals("none"))
                jsch.addIdentity(externalServer.getPrivateKeyFile());
            session = jsch.getSession(externalServer.getUsername(), externalServer.getIp(), externalServer.getPort());
            if (externalServer.getPassword() != null && !externalServer.getPassword().equals("none"))
                session.setPassword(externalServer.getPassword());
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(10000);
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
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }

    public void executeSSHCommand(HoldServer externalServer, String command, Runnable onSuccess, Runnable onError) {
        Session session = null;
        ChannelExec channel = null;
        try {
            JSch jsch = new JSch();
            if (externalServer.getPrivateKeyFile() != null && !externalServer.getPrivateKeyFile().equals("none"))
                jsch.addIdentity(externalServer.getPrivateKeyFile());
            session = jsch.getSession(externalServer.getUsername(), externalServer.getIp(), externalServer.getPort());
            if (externalServer.getPassword() != null && !externalServer.getPassword().equals("none"))
                session.setPassword(externalServer.getPassword());
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect(10000);
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
                onError.run();
            }
        } catch (Exception e) {
            onError.run();
        } finally {
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }
}