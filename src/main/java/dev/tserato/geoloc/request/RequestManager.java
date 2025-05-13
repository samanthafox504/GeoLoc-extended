package dev.tserato.geoloc.request;

import com.google.gson.Gson;
import dev.tserato.geoloc.GeoLocation;
import dev.tserato.geoloc.config.DefaultLocationValueHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestManager {

    public static GeoLocation getGeoLocationData(String ipAddress) {
        try {
            String urlString = "http://ip-api.com/json/" + ipAddress + "?fields=status,message,continent,continentCode,country,countryCode,region,regionName,city,district,zip,lat,lon,timezone,isp,org,as";
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
            return geoLocation.getContinent() + ", " +
                    geoLocation.getContinentCode() + ", " +
                    geoLocation.getCountry() + ", " +
                    geoLocation.getCountryCode() + ", " +
                    geoLocation.getRegion() + ", " +
                    geoLocation.getRegionCode() + ", " +
                    geoLocation.getCity() + ", " +
                    geoLocation.getLocalTime();
        }
        return DefaultLocationValueHandler.getDefaultLocationValue();
    }

    public static String getCityGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getCity() : DefaultLocationValueHandler.getDefaultLocationValue();
    }

    public static String getRegionGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getRegion() : DefaultLocationValueHandler.getDefaultLocationValue();
    }

    public static String getRegionCodeGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getRegionCode() : DefaultLocationValueHandler.getDefaultLocationValue();
    }

    public static String getCountryGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getCountry() : DefaultLocationValueHandler.getDefaultLocationValue();
    }
    public static String getCountryCodeGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getCountryCode() : DefaultLocationValueHandler.getDefaultLocationValue();
    }
    public static String getContinentGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getContinent() : DefaultLocationValueHandler.getDefaultLocationValue();
    }
    public static String getContinentCodeGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getContinentCode() : DefaultLocationValueHandler.getDefaultLocationValue();
    }

    public static String getLocalTimeGeoLocation(String ipAddress) {
        GeoLocation geoLocation = getGeoLocationData(ipAddress);
        return (geoLocation != null) ? geoLocation.getLocalTime() : DefaultLocationValueHandler.getDefaultLocationValue();
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
