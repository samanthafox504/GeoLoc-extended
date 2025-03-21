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
                                                prefix + "&cPlayer &e" + playerName + " &cnot found or offline.");
                                        ctx.getSource().getSender().sendMessage(errorMessage);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    // Get player's IP address
                                    InetSocketAddress ipSocket = targetPlayer.getAddress();
                                    if (ipSocket == null) {
                                        Component errorMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(
                                                prefix + "&cCouldn't get IP address for player &e" + playerName);
                                        ctx.getSource().getSender().sendMessage(errorMessage);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    String ip = ipSocket.getAddress().getHostAddress();

                                    // Get geolocation data
                                    GeoLocation geoLocation = RequestManager.getGeoLocationData(ip);

                                    if (geoLocation == null) {
                                        Component errorMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(
                                                prefix + "&cCouldn't get geolocation data for &e" + playerName);
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