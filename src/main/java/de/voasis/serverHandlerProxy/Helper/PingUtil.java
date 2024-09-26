package de.voasis.serverHandlerProxy.Helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Callable;

public class PingUtil {
    static DataHolder dataHolder;
    static ProxyServer server;
    static Logger logger;
    static Object plugin;

    public PingUtil(DataHolder dataHolder, ProxyServer server, Logger logger, Object plugin) {
        PingUtil.dataHolder = dataHolder;
        PingUtil.server = server;
        PingUtil.logger = logger;
        PingUtil.plugin = plugin;
    }
    public void updateState() {
        for(BackendServer backendServer : dataHolder.backendInfoMap) {
            Optional<RegisteredServer> r = server.getServer(backendServer.getServerName());
            r.ifPresent(registeredServer -> pingServer(registeredServer, stateComplete(registeredServer), stateCompleteFailed(registeredServer), logger, plugin));
        }
    }
    public Callable<Void> stateComplete(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (!backendServer.getState()) {
                        backendServer.setState(true);
                        logger.info("Server: " + backendServer.getServerName() + ", is now online.");
                        CommandSource creator = backendServer.getCreator();
                        if(creator != null) {
                            creator.sendMessage(Component.text("Server: " + backendServer.getServerName() + ", is now online.", NamedTextColor.GREEN));
                        }
                    }
                }
            }
            return null;
        };
    }
    public Callable<Void> stateCompleteFailed(RegisteredServer registeredServer) {
        return () -> {
            for (BackendServer backendServer : dataHolder.backendInfoMap) {
                if (registeredServer.getServerInfo().getName().equals(backendServer.getServerName())) {
                    if (backendServer.getState()) {
                        backendServer.setState(false);
                        CommandSource creator = backendServer.getCreator();
                        logger.info("Server: " + backendServer.getServerName() + ", is now offline.");
                        if(creator != null) {
                            creator.sendMessage(Component.text("Server: " + backendServer.getServerName() + ", is now offline.", NamedTextColor.GREEN));
                        }
                    }
                }
            }
            return null;
        };
    }
    public void pingServer(RegisteredServer regServer, Callable<Void> response, Callable<Void> noResponse, Logger logger, Object plugin) {
        regServer.ping().whenComplete((result, exception) -> {
            if (exception == null) {
                try {
                    synchronized (plugin) {
                        response.call();
                    }
                } catch (Exception e) {
                    logger.error("Error while executing success response for server: " + regServer.getServerInfo().getName(), e);
                }
            } else {
                try {
                    synchronized (plugin) {
                        noResponse.call();
                    }
                } catch (Exception e) {
                    logger.error("Error while executing failure response for server: " + regServer.getServerInfo().getName(), e);
                }
            }
        });
    }
    public void updateFreePort(ServerInfo externalServer) {
        int freePort = -1;
        try {
            String urlString = "http://" + externalServer.getIp() + ":" + externalServer.getPort() + "/freeport";
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("password", externalServer.getPassword().trim());
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    if (jsonResponse.has("success") && jsonResponse.get("success").getAsBoolean()) {
                        freePort = jsonResponse.get("message").getAsInt();
                        logger.info("Free port received: " + freePort);
                    } else {
                        logger.error("Failed to get free port from external server.");
                    }
                }
            } else {
                logger.error("Failed to request free port. Response Code: " + responseCode);
            }
        } catch (Exception ignored) {
            logger.error("Exception occurred while requesting free port");
        }
        answerFreePort(externalServer, freePort);
    }

    private void answerFreePort(ServerInfo externalServer, int freePort) {
        if (freePort != -1) {
            externalServer.setFreePort(freePort);
        } else {
            logger.error("Failed to receive free port from external server.");
        }
    }
}
