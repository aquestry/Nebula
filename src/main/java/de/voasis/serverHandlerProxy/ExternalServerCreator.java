package de.voasis.serverHandlerProxy;

import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.voasis.serverHandlerProxy.Maps.BackendServer;
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

    public void createFromTemplate(ServerInfo externalServer, String templateName, String newName, String startCMD, String stopCMD) {
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
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Instance created successfully from template.");
                int tempPort = Integer.parseInt(startCMD.split("-p")[1].trim());
                com.velocitypowered.api.proxy.server.ServerInfo newInfo = new com.velocitypowered.api.proxy.server.ServerInfo(
                        newName, new InetSocketAddress(externalServer.getIp(), tempPort));
                server.registerServer(newInfo);
                dataHolder.backendInfoMap.add(new BackendServer(newName, externalServer.getServerName(), tempPort, false));
            } else {
                logger.info("Failed to create instance from template. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.info("Error while creating the instance from template.");
            e.printStackTrace();
        }
    }
    public void start(ServerInfo externalServer, String servername) {
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
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.info("Failed to start instance. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.info("Error while starting the instance.");
            e.printStackTrace();
        }
    }
    public void delete(ServerInfo externalServer, String servername) {
        disconnectAll(servername);
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
            String response = connection.getResponseMessage();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Instance deleted successfully. Response: " + response);
                com.velocitypowered.api.proxy.server.ServerInfo info = null;
                for (RegisteredServer i : server.getAllServers()) {
                    if(i.getServerInfo().getName().equals(servername)) {
                        info = i.getServerInfo();
                    }
                }
                if(info != null) {
                    server.unregisterServer(info);
                    dataHolder.backendInfoMap.removeIf(back -> back.getServerName().equals(servername));
                }
            } else {
                logger.info("Failed to delete instance. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.info("Error while deleting the instance.");
            e.printStackTrace();
        }
    }
    public void stop(ServerInfo externalServer, String servername) {
        disconnectAll(servername);
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
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.info("Failed to stop instance. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.info("Error while stopping the instance.");
            e.printStackTrace();
        }
    }
    public void disconnectAll(String backendServer) {
        logger.info("Sending all to default server. Server: " + backendServer);
        Optional<RegisteredServer> r = server.getServer(backendServer);
        if(r.isPresent()) {
            for (Player player : r.get().getPlayersConnected()) {
                if(dataHolder.getState(dataHolder.defaultServer)) {
                    player.disconnect(Component.text("The Server you were on was deleted.", NamedTextColor.RED));
                }
            }
        }
    }
}
