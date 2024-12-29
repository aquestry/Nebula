package de.voasis.nebula.helper;

import de.voasis.nebula.map.HoldServer;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.EnumSet;

public class SSH {
    private ClientSession createSSHClient(HoldServer server) throws IOException {
        SshClient client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier((clientSession, remoteAddress, serverKey) -> true);
        client.start();
        ConnectFuture connectFuture = client.connect(server.getUsername(), server.getIp(), server.getPort());
        connectFuture.await(Duration.ofSeconds(5));
        ClientSession session = connectFuture.getSession();
        if (server.getPrivateKeyFile() != null && !server.getPrivateKeyFile().equals("none")) {
            FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(Path.of(server.getPrivateKeyFile()));
            session.addPublicKeyIdentity(keyPairProvider.loadKeys(session).iterator().next());
            session.auth().verify(Duration.ofSeconds(5));
        } else if (server.getPassword() != null && !server.getPassword().equals("none")) {
            session.addPasswordIdentity(server.getPassword());
            session.auth().verify(Duration.ofSeconds(5));
        } else {
            throw new IOException("No valid authentication method provided.");
        }
        return session;
    }
    public void updateFreePort(HoldServer externalServer) {
        int freePort = -1;
        try (ClientSession session = createSSHClient(externalServer); ChannelExec channel = session.createExecChannel("ruby -e 'require \"socket\"; puts Addrinfo.tcp(\"\", 0).bind {|s| s.local_address.ip_port }'")) {
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOut(responseStream);
            channel.open().verify(Duration.ofSeconds(5));
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), Duration.ofSeconds(5).toMillis());
            String output = responseStream.toString().trim();
            if (!output.isEmpty()) {
                freePort = Integer.parseInt(output);
            }
            externalServer.setFreePort(Math.max(freePort, 0));
        } catch (IOException ignored) {}
    }
    public void executeSSHCommand(HoldServer externalServer, String command, Runnable onSuccess, Runnable onError) {
        try (ClientSession session = createSSHClient(externalServer); ChannelExec channel = session.createExecChannel(command)) {
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOut(responseStream);
            channel.open().verify(Duration.ofSeconds(5));
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), Duration.ofSeconds(5).toMillis());
            Integer exitStatus = channel.getExitStatus();
            if (exitStatus != null && exitStatus == 0) {
                onSuccess.run();
            } else {
                onError.run();
            }
        } catch (IOException e) {
            onError.run();
        }
    }
}