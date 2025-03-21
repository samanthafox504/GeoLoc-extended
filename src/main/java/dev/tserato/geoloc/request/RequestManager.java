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

    public static String getFullGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        if (geoLocation != null) {
            return geoLocation.getCountry() + ", " +
                    geoLocation.getRegion() + ", " +
                    geoLocation.getCity() + ", " +
                    geoLocation.getLocalTime();
        }
        return "Unknown";
    }

    public static String getCityGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getCity() : "Unknown";
    }

    public static String getRegionGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getRegion() : "Unknown";
    }

    public static String getCountryGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getCountry() : "Unknown";
    }

    public static String getLocalTimeGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getLocalTime() : "Unknown";
    }

    public static String getPartialGeoLocation(String ipAddress, boolean includeCountry,
                                               boolean includeRegion, boolean includeCity,
                                               boolean includeLocalTime) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        if (geoLocation != null) {
            StringBuilder result = new StringBuilder();
            boolean needsComma = false;

            if (includeCountry) {
                result.append(geoLocation.getCountry());
                needsComma = true;
            }

            if (includeRegion) {
                if (needsComma) result.append(", ");
                result.append(geoLocation.getRegion());
                needsComma = true;
            }

            if (includeCity) {
                if (needsComma) result.append(", ");
                result.append(geoLocation.getCity());
                needsComma = true;
            }

            if (includeLocalTime) {
                if (needsComma) result.append(", ");
                result.append(geoLocation.getLocalTime());
            }

            return result.toString();
        }
        return "Unknown";
    }
}
