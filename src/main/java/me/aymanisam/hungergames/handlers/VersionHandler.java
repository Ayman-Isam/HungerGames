package me.aymanisam.hungergames.handlers;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class VersionHandler {

    private static final String SPIGET_API_URL = "https://api.spiget.org/v2/resources/%d/versions/latest";

    public static String getLatestPluginVersion(int pluginId) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(SPIGET_API_URL, pluginId)))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JSONParser parser = new JSONParser();
                JSONObject jsonResponse = (JSONObject) parser.parse(response.body());
                return (String) jsonResponse.get("name");
            } else {
                return "Failed to get version: " + response.statusCode();
            }
        } catch (IOException | InterruptedException | ParseException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
