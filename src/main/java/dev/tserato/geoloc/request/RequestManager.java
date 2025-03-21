package dev.tserato.geoloc.request;

import com.google.gson.Gson;
import dev.tserato.geoloc.GeoLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestManager {

    public static GeoLocation getGeoLocationData(String ipAddress) {
        try {
            String urlString = "http://ip-api.com/json/" + ipAddress;
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            if (connection.getResponseCode() == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    return new Gson().fromJson(in.readLine(), GeoLocation.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        if (geoLocation != null) {
            return geoLocation.getCountry() + ", " + geoLocation.getRegion() + ", " + geoLocation.getCity() +
                    " (Local time: " + geoLocation.getLocalTime() + ")";
        }
        return "Unknown";
    }
}