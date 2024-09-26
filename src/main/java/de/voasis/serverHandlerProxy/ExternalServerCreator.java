package de.voasis.serverHandlerProxy;

import com.google.gson.JsonObject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Helper.DataHolder;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
import de.voasis.serverHandlerProxy.Maps.Messages;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class ExternalServerCreator {
    private Logger logger;
    private ProxyServer server;
    private DataHolder dataHolder;

    public ExternalServerCreator(Logger logger, ProxyServer proxyServer, DataHolder dataHolder) {
        this.logger = logger;
        this.server = proxyServer;
        this.dataHolder = dataHolder;
    }

    public void createFromTemplate(ServerInfo externalServer, String templateName, String newName, String startCMD, String stopCMD, CommandSource source) {
        try {
            String urlString = "http://" + externalServer.getIp() + ":" + externalServer.getPort() + "/create";
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("template", templateName);
            jsonRequest.addProperty("name", newName);
            jsonRequest.addProperty("start_cmd", startCMD);
            jsonRequest.addProperty("stop_cmd", stopCMD);
            jsonRequest.addProperty("password", externalServer.getPassword().trim());

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            logger.info("Response code: " + responseCode + ", Response message: " + connection.getResponseMessage());

            if (responseCode == HttpURLConnection.HTTP_OK) {
                sendSuccessMessage(source, "Instance-Template request successfully sent.");
                int tempPort;
                try {
                    String[] splitCmd = startCMD.split("-p");
                    if (splitCmd.length > 1) {
                        tempPort = Integer.parseInt(splitCmd[1].trim());
                    } else {
                        logger.error("Failed to extract port from startCMD: " + startCMD);
                        sendErrorMessage(source, "Failed to extract port from startCMD.");
                        return;
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid port number in startCMD: " + startCMD, e);
                    sendErrorMessage(source, "Invalid port number in startCMD.");
                    return;
                }

                com.velocitypowered.api.proxy.server.ServerInfo newInfo = new com.velocitypowered.api.proxy.server.ServerInfo(
                        newName, new InetSocketAddress(externalServer.getIp(), tempPort));
                server.registerServer(newInfo);

                Optional<RegisteredServer> registeredServer = server.getServer(newName);
                if (registeredServer.isPresent()) {
                    logger.info("Server successfully registered: " + newName + " at port " + tempPort);
                    dataHolder.backendInfoMap.add(new BackendServer(newName, externalServer.getServerName(), tempPort, false));
                } else {
                    logger.error("Failed to register the server: " + newName);
                    sendErrorMessage(source, "Failed to register the server: " + newName);
                }
            } else {
                logger.error("Failed to create instance from template. Response Code: " + responseCode);
                sendErrorMessage(source, "Failed to create instance from template. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while creating instance from template", e);
            sendErrorMessage(source, "Exception occurred while creating instance from template.");
        }
    }

    public void start(ServerInfo externalServer, String servername, CommandSource source) {
        try {
            String urlString = "http://" + externalServer.getIp() + ":" + externalServer.getPort() + "/start/" + servername;
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
                sendSuccessMessage(source, "Instance successfully started.");
            } else {
                logger.error("Failed to start instance. Response Code: " + responseCode);
                sendErrorMessage(source, "Failed to start instance. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while starting instance.", e);
            sendErrorMessage(source, "Exception occurred while starting instance.");
        }
    }

    public void stop(ServerInfo externalServer, String servername, CommandSource source) {
        disconnectAll(servername, Messages.stopped);
        try {
            String urlString = "http://" + externalServer.getIp() + ":" + externalServer.getPort() + "/kill/" + servername;
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
                sendSuccessMessage(source, "Instance successfully stopped.");
            } else {
                logger.error("Failed to stop instance. Response Code: " + responseCode);
                sendErrorMessage(source, "Failed to stop instance. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while stopping instance.", e);
            sendErrorMessage(source, "Exception occurred while stopping instance.");
        }
    }

    public void delete(ServerInfo externalServer, String servername, CommandSource source) {
        disconnectAll(servername, Messages.deleted);
        try {
            String urlString = "http://" + externalServer.getIp() + ":" + externalServer.getPort() + "/delete/" + servername;
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
                sendSuccessMessage(source, "Instance successfully deleted.");
                server.getServer(servername).ifPresent(s -> server.unregisterServer(s.getServerInfo()));
                dataHolder.backendInfoMap.removeIf(back -> back.getServerName().equals(servername));
            } else {
                logger.error("Failed to delete instance. Response Code: " + responseCode);
                sendErrorMessage(source, "Failed to delete instance. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.error("Exception occurred while deleting instance.", e);
            sendErrorMessage(source, "Exception occurred while deleting instance.");
        }
    }

    private void sendSuccessMessage(CommandSource source, String message) {
        logger.info(message);
        if (source != null) {
            source.sendMessage(Component.text(message, NamedTextColor.GREEN));
        }
    }

    private void sendErrorMessage(CommandSource source, String message) {
        logger.error(message);
        if (source != null) {
            source.sendMessage(Component.text(message, NamedTextColor.RED));
        }
    }

    public void disconnectAll(String backendServer, String reason) {
        logger.info("Sending all to default server. Server: " + backendServer);
        Optional<RegisteredServer> r = server.getServer(backendServer);
        if (r.isPresent()) {
            for (Player player : r.get().getPlayersConnected()) {
                if (dataHolder.getState(dataHolder.defaultServer)) {
                    player.disconnect(Component.text(reason, NamedTextColor.RED));
                }
            }
        }
    }
}
