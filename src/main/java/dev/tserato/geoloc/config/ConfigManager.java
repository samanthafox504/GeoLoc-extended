package dev.tserato.geoloc.config;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.tserato.geoloc.GeoLoc;
import dev.tserato.geoloc.GeoLocation;
import dev.tserato.geoloc.request.RequestManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setUp();
    }

    public void setUp() {
        // Set up main config
        plugin.saveDefaultConfig();
        this.config = plugin.getConfig();

        // Set up messages config
        createMessagesConfig();
    }

    private void createMessagesConfig() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");

        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        // Load default messages from resource if available
        InputStream defaultMessages = plugin.getResource("messages.yml");
        if (defaultMessages != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultMessages, StandardCharsets.UTF_8));
            messagesConfig.setDefaults(defaultConfig);
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
                                .executes(ctx -> {
                                    // Handle /geoloc geolocation <player> - return full geolocation data
                                    return handleGeoLocationCommand(ctx, configManager, plugin, "full", false);
                                })
                        )
                        .then(Commands.literal("full")
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
                                            // Handle /geoloc geolocation full <player> - return full geolocation
                                            return handleGeoLocationCommand(ctx, configManager, plugin, "full", true);
                                        })
                                )
                        )
                        .then(Commands.literal("city")
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
                                            // Handle /geoloc geolocation city <player> - return city geolocation
                                            return handleGeoLocationCommand(ctx, configManager, plugin, "city", true);
                                        })
                                )
                        )
                        .then(Commands.literal("region")
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
                                            // Handle /geoloc geolocation region <player> - return region geolocation
                                            return handleGeoLocationCommand(ctx, configManager, plugin, "region", true);
                                        })
                                )
                        )
                        .then(Commands.literal("country")
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
                                            // Handle /geoloc geolocation country <player> - return country geolocation
                                            return handleGeoLocationCommand(ctx, configManager, plugin, "country", true);
                                        })
                                )
                        )
                        .then(Commands.literal("localTime")
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
                                            // Handle /geoloc geolocation localTime <player> - return local time geolocation
                                            return handleGeoLocationCommand(ctx, configManager, plugin, "localTime", true);
                                        })
                                )
                        )
                )
                .build();
    }

    // Helper method to handle all geolocation commands
    private static int handleGeoLocationCommand(com.mojang.brigadier.context.CommandContext<CommandSourceStack> ctx,
                                                ConfigManager configManager,
                                                GeoLoc plugin,
                                                String infoType,
                                                boolean suggestPlayers) {
        String playerName = StringArgumentType.getString(ctx, "player");
        Player targetPlayer = plugin.getServer().getPlayer(playerName);

        String prefix = configManager.getPrefix();
        if (targetPlayer == null) {
            // Player not found
            String playerNotFoundMessageTemplate = configManager.getMessage("error.player-not-found");
            String errorMessage = prefix + playerNotFoundMessageTemplate.replace("{player}", playerName);
            Component errorComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(errorMessage);
            ctx.getSource().getSender().sendMessage(errorComponent);
            return Command.SINGLE_SUCCESS;
        }

        // Get player's IP address
        InetSocketAddress ipSocket = targetPlayer.getAddress();
        if (ipSocket == null) {
            String ipNotFoundMessageTemplate = configManager.getMessage("error.ip-not-found");
            String errorMessage = prefix + ipNotFoundMessageTemplate.replace("{player}", playerName);
            Component errorComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(errorMessage);
            ctx.getSource().getSender().sendMessage(errorComponent);
            return Command.SINGLE_SUCCESS;
        }

        String ip = ipSocket.getAddress().getHostAddress();

        // Get geolocation data
        GeoLocation geoLocation = RequestManager.getGeoLocationData(ip);

        if (geoLocation == null) {
            String geolocationNotFoundMessageTemplate = configManager.getMessage("error.geolocation-not-found");
            String errorMessage = prefix + geolocationNotFoundMessageTemplate.replace("{player}", playerName);
            Component errorComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(errorMessage);
            ctx.getSource().getSender().sendMessage(errorComponent);
            return Command.SINGLE_SUCCESS;
        }

        // Determine which message template to use based on info type
        String messageKey;
        String value;
        String defaultLocationValue = configManager.getMessage("default-location-value");

        switch (infoType) {
            case "city":
                messageKey = "command.location-city";
                value = geoLocation.getCity() != null ? geoLocation.getCity() : defaultLocationValue;
                break;
            case "region":
                messageKey = "command.location-region";
                value = geoLocation.getRegion() != null ? geoLocation.getRegion() : defaultLocationValue;
                break;
            case "country":
                messageKey = "command.location-country";
                value = geoLocation.getCountry() != null ? geoLocation.getCountry() : defaultLocationValue;
                break;
            case "localTime":
                messageKey = "command.location-time";
                value = geoLocation.getLocalTime() != null ? geoLocation.getLocalTime() : defaultLocationValue;
                break;
            case "full":
            default:
                messageKey = "command.location";
                // For full info, we'll use the existing template with all replacements
                String locationTemplate = configManager.getMessage(messageKey);
                String formattedMessage = locationTemplate
                        .replace("{player}", playerName)
                        .replace("{city}", geoLocation.getCity() != null ? geoLocation.getCity() : defaultLocationValue)
                        .replace("{region}", geoLocation.getRegion() != null ? geoLocation.getRegion() : defaultLocationValue)
                        .replace("{country}", geoLocation.getCountry() != null ? geoLocation.getCountry() : defaultLocationValue)
                        .replace("{localTime}", geoLocation.getLocalTime() != null ? geoLocation.getLocalTime() : defaultLocationValue);

                Component locationMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + formattedMessage);
                ctx.getSource().getSender().sendMessage(locationMessage);
                return Command.SINGLE_SUCCESS;
        }

        // For specific info types (not full)
        String messageTemplate = configManager.getMessage(messageKey);
        String formattedMessage = messageTemplate
                .replace("{player}", playerName)
                .replace("{value}", value);

        Component message = LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + formattedMessage);
        ctx.getSource().getSender().sendMessage(message);

        return Command.SINGLE_SUCCESS;
    }

    public void reloadPluginConfig() {
        // Reload main config
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        // Reload messages config
        if (messagesFile != null) {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

            // Reload defaults
            InputStream defaultMessages = plugin.getResource("messages.yml");
            if (defaultMessages != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultMessages, StandardCharsets.UTF_8));
                messagesConfig.setDefaults(defaultConfig);
            }
        }
    }

    public boolean isJoinMessageEnabled() {
        if (config == null) {
            setUp();
        }
        return config != null && config.getBoolean("join-message-enabled", false);
    }

    public String getMessage(String path) {
        if (messagesConfig == null) {
            createMessagesConfig();
        }

        String defaultMessage = "&cMissing message: " + path;
        return messagesConfig != null ? messagesConfig.getString(path, defaultMessage) : defaultMessage;
    }

    public String getPrefix() {
        return config.getString("prefix", "&8[&bGeoLoc&8] ");
    }
}