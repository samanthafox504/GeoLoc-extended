package dev.tserato.geoloc.expansion;

import dev.tserato.geoloc.config.ConfigManager;
import dev.tserato.geoloc.config.DefaultLocationValueHandler;
import dev.tserato.geoloc.request.RequestManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class PAPIExpansion extends PlaceholderExpansion {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;

    public PAPIExpansion(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "TSERATO";
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "geoloc";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return "Unknown";
        }

        Player player = offlinePlayer.getPlayer();
        assert player != null;

        String ipAddress = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : null;
        if (ipAddress == null) {
            return "Unknown IP";
        }

        return switch (params.toLowerCase()) {
            case "full" -> fullRequest(ipAddress);
            case "city" -> cityRequest(ipAddress);
            case "regionCode" -> regionCodeRequest(ipAddress);
            case "region" -> regionRequest(ipAddress);
            case "countryCode" -> countryCodeRequest(ipAddress);
            case "country" -> countryRequest(ipAddress);
            case "continentCode" -> continentCodeRequest(ipAddress);
            case "continent" -> continentRequest(ipAddress);
            case "localtime" -> localTimeRequest(ipAddress);
            default -> "Invalid Placeholder";
        };
    }

    public String fullRequest(String ipAddress) {
        String fullRequest = RequestManager.getFullGeoLocation(ipAddress);

        // Handle cases where the request fails
        if (fullRequest.equals("Unknown")) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        // Splitting the fullRequest string into parts
        String[] parts = fullRequest.split(", ");

        // Ensure we have all parts
        String continent = parts.length > 0 ? parts[0] : DefaultLocationValueHandler.getDefaultLocationValue();
        String continentCode = parts.length > 1 ? parts[1] : DefaultLocationValueHandler.getDefaultLocationValue();
        String country = parts.length > 2 ? parts[2] : DefaultLocationValueHandler.getDefaultLocationValue();
        String countryCode = parts.length > 3 ? parts[3] : DefaultLocationValueHandler.getDefaultLocationValue();
        String region = parts.length > 4 ? parts[4] : DefaultLocationValueHandler.getDefaultLocationValue();
        String regionCode = parts.length > 5 ? parts[5] : DefaultLocationValueHandler.getDefaultLocationValue();
        String city = parts.length > 6 ? parts[6] : DefaultLocationValueHandler.getDefaultLocationValue();
        String localTime = parts.length > 7 ? parts[7] : DefaultLocationValueHandler.getDefaultLocationValue();

        // Retrieve formatted message from config
        String fullMessageTemplate = configManager.getMessage("placeholder.full");

        // Replace placeholders with actual values
        return fullMessageTemplate
                .replace("{continent}", continent)
                .replace("{continentCode}", continentCode)
                .replace("{country}", country)
                .replace("{countryCode}", countryCode)
                .replace("{region}", region)
                .replace("{regionCode}", regionCode)
                .replace("{city}", city)
                .replace("{localtime}", localTime);
    }

    public String cityRequest(String ipAddress) {
        String fullRequest = RequestManager.getFullGeoLocation(ipAddress);

        // Handle cases where the request fails
        if (fullRequest.equals("Unknown")) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        // Splitting the fullRequest string into parts
        String[] parts = fullRequest.split(", ");

        String city = parts.length > 6 ? parts[6] : DefaultLocationValueHandler.getDefaultLocationValue();

        // Retrieve formatted message from config
        String cityMessageTemplate = configManager.getMessage("placeholder.city");

        // Replace placeholders with actual values
        return cityMessageTemplate
                .replace("{city}", city);
    }

    public String regionRequest(String ipAddress) {
        String fullRequest = RequestManager.getFullGeoLocation(ipAddress);

        // Handle cases where the request fails
        if (fullRequest.equals("Unknown")) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        // Splitting the fullRequest string into parts
        String[] parts = fullRequest.split(", ");

        String region = parts.length > 4 ? parts[4] : DefaultLocationValueHandler.getDefaultLocationValue();

        // Retrieve formatted message from config
        String regionMessageTemplate = configManager.getMessage("placeholder.region");

        // Replace placeholders with actual values
        return regionMessageTemplate
                .replace("{region}", region);
    }
    public String regionCodeRequest(String ipAddress) {
        String fullRequest = RequestManager.getFullGeoLocation(ipAddress);

        // Handle cases where the request fails
        if (fullRequest.equals("Unknown")) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        // Splitting the fullRequest string into parts
        String[] parts = fullRequest.split(", ");

        String regionCode = parts.length > 5 ? parts[5] : DefaultLocationValueHandler.getDefaultLocationValue();

        // Retrieve formatted message from config
        String regionMessageTemplate = configManager.getMessage("placeholder.region");

        // Replace placeholders with actual values
        return regionMessageTemplate
                .replace("{region}", regionCode);
    }

    public String countryRequest(String ipAddress) {
        String fullRequest = RequestManager.getFullGeoLocation(ipAddress);

        // Handle cases where the request fails
        if (fullRequest.equals("Unknown")) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        // Splitting the fullRequest string into parts
        String[] parts = fullRequest.split(", ");

        String country = parts.length > 2 ? parts[2] : DefaultLocationValueHandler.getDefaultLocationValue();

        // Retrieve formatted message from config
        String countryMessageTemplate = configManager.getMessage("placeholder.country");

        // Replace placeholders with actual values
        return countryMessageTemplate
                .replace("{country}", country);
    }
    public String countryCodeRequest(String ipAddress) {
        String fullRequest = RequestManager.getFullGeoLocation(ipAddress);

        // Handle cases where the request fails
        if (fullRequest.equals("Unknown")) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        // Splitting the fullRequest string into parts
        String[] parts = fullRequest.split(", ");

        String countryCode = parts.length > 3 ? parts[3] : DefaultLocationValueHandler.getDefaultLocationValue();

        // Retrieve formatted message from config
        String countryCodeMessageTemplate = configManager.getMessage("placeholder.countryCode");

        // Replace placeholders with actual values
        return countryCodeMessageTemplate
                .replace("{country}", countryCode);
    }
    public String continentRequest(String ipAddress) {
        String fullRequest = RequestManager.getFullGeoLocation(ipAddress);

        // Handle cases where the request fails
        if (fullRequest.equals("Unknown")) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        // Splitting the fullRequest string into parts
        String[] parts = fullRequest.split(", ");

        String continent = parts.length > 0 ? parts[0] : DefaultLocationValueHandler.getDefaultLocationValue();

        // Retrieve formatted message from config
        String continentMessageTemplate = configManager.getMessage("placeholder.continent");

        // Replace placeholders with actual values
        return continentMessageTemplate
                .replace("{continent}", continent);
    }
    public String continentCodeRequest(String ipAddress) {
        String fullRequest = RequestManager.getFullGeoLocation(ipAddress);

        // Handle cases where the request fails
        if (fullRequest.equals("Unknown")) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        // Splitting the fullRequest string into parts
        String[] parts = fullRequest.split(", ");

        String continentCode = parts.length > 1 ? parts[1] : DefaultLocationValueHandler.getDefaultLocationValue();

        // Retrieve formatted message from config
        String continentCodeMessageTemplate = configManager.getMessage("placeholder.continentCode");

        // Replace placeholders with actual values
        return continentCodeMessageTemplate
                .replace("{continentCode}", continentCode);
    }


    public String localTimeRequest(String ipAddress) {
        String fullRequest = RequestManager.getFullGeoLocation(ipAddress);

        // Handle cases where the request fails
        if (fullRequest.equals("Unknown")) {
            return DefaultLocationValueHandler.getDefaultLocationValue();
        }

        // Splitting the fullRequest string into parts
        String[] parts = fullRequest.split(", ");

        String localTime = parts.length > 7 ? parts[7] : DefaultLocationValueHandler.getDefaultLocationValue();

        // Retrieve formatted message from config
        String localTimeMessageTemplate = configManager.getMessage("placeholder.localTime");

        // Replace placeholders with actual values
        return localTimeMessageTemplate
                .replace("{localTime}", localTime);
    }
}
