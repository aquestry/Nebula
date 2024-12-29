package de.voasis.nebula.helper;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import de.voasis.nebula.Nebula;
import de.voasis.nebula.map.HoldServer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SSH {

    private SSHClient createSSHClient(HoldServer server) throws IOException {
        Nebula.util.log("🔄 Creating SSH session for server: {}", server.getServerName());
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(server.getIp(), server.getPort());

        if (server.getPrivateKeyFile() != null && !server.getPrivateKeyFile().equals("none")) {
            Nebula.util.log("🔑 Using Private Key for authentication: {}", server.getPrivateKeyFile());
            ssh.authPublickey(server.getUsername(), server.getPrivateKeyFile());
        } else if (server.getPassword() != null && !server.getPassword().equals("none")) {
            Nebula.util.log("🔑 Using Password for authentication");
            ssh.authPassword(server.getUsername(), server.getPassword());
        } else {
            throw new IOException("❌ No valid authentication method provided.");
        }

        Nebula.util.log("✅ SSH session connected.");
        return ssh;
    }

    public void updateFreePort(HoldServer externalServer) {
        int freePort = -1;
        SSHClient ssh = null;
        Session session = null;
        try {
            Nebula.util.log("🔄 Starting updateFreePort for server '{}'", externalServer.getServerName());
            ssh = createSSHClient(externalServer);
            session = ssh.startSession();

            Nebula.util.log("🔄 Executing command to fetch free port...");
            Session.Command cmd = session.exec("ruby -e 'require \"socket\"; puts Addrinfo.tcp(\"\", 0).bind {|s| s.local_address.ip_port }'");
            cmd.join(5, TimeUnit.SECONDS);
            String output = new String(cmd.getInputStream().readAllBytes()).trim();

            Nebula.util.log("🔄 Command Output: {}", output);

            if (!output.isEmpty()) {
                freePort = Integer.parseInt(output);
                Nebula.util.log("✅ Free port found: {}", freePort);
            } else {
                Nebula.util.log("❌ No free port found in command output.");
            }

            externalServer.setFreePort(Math.max(freePort, 0));
        } catch (IOException e) {
            Nebula.util.log("❌ SSH Error: {}", e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (session != null) session.close();
                if (ssh != null) ssh.disconnect();
            } catch (IOException e) {
                Nebula.util.log("❌ Error during cleanup: {}", e.getMessage());
            }
        }
    }

    public void executeSSHCommand(HoldServer externalServer, String command, Runnable onSuccess, Runnable onError) {
        SSHClient ssh = null;
        Session session = null;
        try {
            Nebula.util.log("🔄 Starting executeSSHCommand for server '{}'", externalServer.getServerName());
            ssh = createSSHClient(externalServer);
            session = ssh.startSession();
            Nebula.util.log("🔄 Executing command: {}", command);
            Session.Command cmd = session.exec(command);
            cmd.join(5, TimeUnit.SECONDS);
            int exitStatus = cmd.getExitStatus();
            Nebula.util.log("🔄 Command finished with exit status: {}", exitStatus);
            if (exitStatus == 0) {
                Nebula.util.log("✅ Command executed successfully on server '{}'.", externalServer.getServerName());
                onSuccess.run();
            } else {
                Nebula.util.log("❌ Command failed with exit status {} on server '{}'.", exitStatus, externalServer.getServerName());
                onError.run();
            }
        } catch (IOException e) {
            Nebula.util.log("❌ SSH Connection Error: {}", e.getMessage());
            e.printStackTrace();
            onError.run();
        } finally {
            try {
                if (session != null) session.close();
                if (ssh != null) ssh.disconnect();
            } catch (IOException e) {
                Nebula.util.log("❌ Error during cleanup: {}", e.getMessage());
            }
        }
    }
}
