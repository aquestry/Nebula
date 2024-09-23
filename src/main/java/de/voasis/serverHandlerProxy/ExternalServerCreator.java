package de.voasis.serverHandlerProxy;

import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.ProxyServer;
import de.voasis.serverHandlerProxy.Maps.ServerInfo;
import org.slf4j.Logger;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ExternalServerCreator {
    private Logger logger;
    private ProxyServer server;
    private DataHolder dataHolder;
    public ExternalServerCreator(Logger logger, ProxyServer proxyServer, DataHolder dataHolder) {
        this.logger = logger;
        this.server = proxyServer;
        this.dataHolder = dataHolder;
    }

    public void create(ServerInfo externalServer, String newName, String startCMD, String stopCMD) {
        try {
            String urlString = "http://" + externalServer.getIp() + ":" + externalServer.getPort() + "/create";
            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            JsonObject jsonRequest = new JsonObject();
            jsonRequest.addProperty("serverName", externalServer.getServerName());
            jsonRequest.addProperty("ip", externalServer.getIp());
            jsonRequest.addProperty("port", externalServer.getPort());
            jsonRequest.addProperty("password", externalServer.getPassword());
            jsonRequest.addProperty("name", newName);
            jsonRequest.addProperty("start_cmd", startCMD);
            jsonRequest.addProperty("stop_cmd", stopCMD);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonRequest.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Instance created successfully.");
            } else {
                logger.info("Failed to create instance. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.info("Error while creating the instance.");
            e.printStackTrace();
        }
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
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logger.info("Instance started successfully.");
            } else {
                logger.info("Failed to start instance. Response Code: " + responseCode);
            }

        } catch (Exception e) {
            logger.info("Error while starting the instance.");
            e.printStackTrace();
        }
    }

}
