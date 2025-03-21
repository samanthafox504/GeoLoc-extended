package dev.tserato.geoloc.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tserato.geoloc.GeoLoc;
import dev.tserato.geoloc.GeoLocation;
import dev.tserato.geoloc.language.LanguageManager;
import dev.tserato.geoloc.request.RequestManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.net.InetSocketAddress;

public class ConfigManager {
    private final GeoLoc plugin;
    private FileConfiguration config;
    private LanguageManager languageManager;

    public ConfigManager(GeoLoc plugin) {
        this.plugin = plugin;
        reloadPluginConfig();
    }

    public void reloadPluginConfig() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();

        // Reload config
        plugin.reloadConfig();
        config = plugin.getConfig();

        // Get language settings
        String defaultLang = config.getString("language.default", "en");

        // Initialize or reload language manager
        if (languageManager == null) {
            languageManager = new LanguageManager(plugin, defaultLang);
        } else {
            languageManager.reloadLanguages();
        }
    }

    public static LiteralCommandNode<CommandSourceStack> createGeoLocCommand(ConfigManager configManager, GeoLoc plugin) {
        return Commands.literal("geoloc")
                .requires(source -> source.getSender().hasPermission("geoloc.admin"))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            configManager.reloadPluginConfig();
                            String prefix = configManager.getPrefix();
                            Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + configManager.getMessage("command.reload-success"));
                            ctx.getSource().getSender().sendMessage(message);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("geolocation")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    // Get all online players and suggest them
                                    plugin.getServer().getOnlinePlayers().forEach(player -> {
                                        if (player.getName().toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                                            builder.suggest(player.getName());
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .executes(ctx -> {
                                    String playerName = StringArgumentType.getString(ctx, "player");
                                    Player targetPlayer = plugin.getServer().getPlayer(playerName);

                                    String prefix = configManager.getPrefix();
                                    if (targetPlayer == null) {
                                        // Player not found
                                        Component errorMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(
                                                prefix + configManager.getMessage("command.player-not-found")
                                                        .replace("{player}", playerName));
                                        ctx.getSource().getSender().sendMessage(errorMessage);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    // Get player's IP address
                                    InetSocketAddress ipSocket = targetPlayer.getAddress();
                                    if (ipSocket == null) {
                                        Component errorMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(
                                                prefix + configManager.getMessage("command.no-ip")
                                                        .replace("{player}", playerName));
                                        ctx.getSource().getSender().sendMessage(errorMessage);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String ip = ipSocket.getAddress().getHostAddress();

                                    // Get geolocation data
                                    GeoLocation geoLocation = RequestManager.getGeoLocationData(ip);

                                    if (geoLocation == null) {
                                        Component errorMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(
                                                prefix + configManager.getMessage("command.no-geolocation")
                                                        .replace("{player}", playerName));
                                        ctx.getSource().getSender().sendMessage(errorMessage);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    // Get location message template and replace placeholders
                                    String locationTemplate = configManager.getMessage("command.location");
                                    String formattedMessage = locationTemplate
                                            .replace("{player}", playerName)
                                            .replace("{city}", geoLocation.getCity() != null ? geoLocation.getCity() : "Unknown")
                                            .replace("{region}", geoLocation.getRegion() != null ? geoLocation.getRegion() : "Unknown")
                                            .replace("{country}", geoLocation.getCountry() != null ? geoLocation.getCountry() : "Unknown")
                                            .replace("{localTime}", geoLocation.getLocalTime() != null ? geoLocation.getLocalTime() : "Unknown");

                                    // Format location message with prefix
                                    Component locationMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(
                                            prefix + formattedMessage);

                                    ctx.getSource().getSender().sendMessage(locationMessage);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .build();
    }

    public boolean isJoinMessageEnabled() {
        return config.getBoolean("display.join-message", true);
    }

    public String getMessage(String path) {
        return languageManager.getMessage(path);
    }

    public String getPrefix() {
        return languageManager.getPrefix();
    }

    public String getDefaultLanguage() {
        return config.getString("language.default", "en");
    }
}