package dev.tserato.geoloc.listener;

import dev.tserato.geoloc.GeoLoc;
import dev.tserato.geoloc.GeoLocation;
import dev.tserato.geoloc.config.ConfigManager;
import dev.tserato.geoloc.request.RequestManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetSocketAddress;

public class EventListener implements Listener {

    private final GeoLoc plugin;
    private final ConfigManager configManager;

    public EventListener(GeoLoc plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        setUp();
    }

    public void setUp() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        InetSocketAddress ipSocket = player.getAddress();

        if (ipSocket == null) {
            plugin.getLogger().warning(configManager.getMessage("log.no-ip").replace("{player}", player.getName()));
            return;
        }

        String ip = ipSocket.getAddress().getHostAddress();

        // Get GeoLocation data object
        GeoLocation geoLocation = RequestManager.getGeoLocationData(ip);

        if (geoLocation == null) {
            plugin.getLogger().warning(configManager.getMessage("log.no-geolocation").replace("{player}", player.getName()));
            return;
        }

        String defaultLocationValue = configManager.getMessage("default-location-value");

        // Always log to console
        plugin.getLogger().info(configManager.getMessage("log.player-connected")
                .replace("{player}", player.getName())
                .replace("{city}", geoLocation.getCity() != null ? geoLocation.getCity() : defaultLocationValue)
                .replace("{region}", geoLocation.getRegion() != null ? geoLocation.getRegion() : defaultLocationValue)
                .replace("{country}", geoLocation.getCountry() != null ? geoLocation.getCountry() : defaultLocationValue)
                .replace("{localTime}", geoLocation.getLocalTime() != null ? geoLocation.getLocalTime() : defaultLocationValue));

        // Send join message if enabled in config
        if (configManager.isJoinMessageEnabled() && player.hasPermission("geoloc.see")) {
            // Get message template from language file
            String prefix = configManager.getPrefix();
            String messageTemplate = configManager.getMessage("join-message");

            // Replace placeholders with actual data
            String formattedMessage = prefix + messageTemplate
                    .replace("{player}", player.getName())
                    .replace("{city}", geoLocation.getCity() != null ? geoLocation.getCity() : defaultLocationValue)
                    .replace("{region}", geoLocation.getRegion() != null ? geoLocation.getRegion() : defaultLocationValue)
                    .replace("{country}", geoLocation.getCountry() != null ? geoLocation.getCountry() : defaultLocationValue)
                    .replace("{localTime}", geoLocation.getLocalTime() != null ? geoLocation.getLocalTime() : defaultLocationValue);

            // Convert to component with color codes
            Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(formattedMessage);

            // Send to all players
            plugin.getServer().broadcast(message);
        }
    }
}