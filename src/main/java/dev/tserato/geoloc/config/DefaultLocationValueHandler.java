package dev.tserato.geoloc.config;

/**
 * Static handler class for managing default location values
 * This class provides access to default location values and responds to config reloads
 */
public class DefaultLocationValueHandler {

    private static ConfigManager configManager;
    private static String defaultLocationValue = "Unknown";

    /**
     * Initialize the handler with a ConfigManager
     * @param configManager The ConfigManager instance to use
     */
    public static void initialize(ConfigManager configManager) {
        DefaultLocationValueHandler.configManager = configManager;
        refreshDefaultValue();
    }

    /**
     * Get the current default location value
     * @return The default location value string
     */
    public static String getDefaultLocationValue() {
        if (configManager == null) {
            return defaultLocationValue;
        }
        return defaultLocationValue;
    }

    /**
     * Refresh the default location value from the configuration
     * This should be called after config reloads
     */
    public static void refreshDefaultValue() {
        if (configManager != null) {
            defaultLocationValue = configManager.getMessage("default-location-value");
        }
    }
}