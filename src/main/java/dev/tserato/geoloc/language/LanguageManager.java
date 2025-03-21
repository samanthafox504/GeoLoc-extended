package dev.tserato.geoloc.language;

import dev.tserato.geoloc.GeoLoc;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private final GeoLoc plugin;
    private final String defaultLanguage;
    private final Map<String, FileConfiguration> languageFiles = new HashMap<>();

    public LanguageManager(GeoLoc plugin, String defaultLanguage) {
        this.plugin = plugin;
        this.defaultLanguage = defaultLanguage;
        loadLanguages();
    }

    private void loadLanguages() {
        // Create the lang directory if it doesn't exist
        File langDir = new File(plugin.getDataFolder(), "lang");
        if (!langDir.exists()) {
            langDir.mkdirs();
            // Save default language files
            plugin.saveResource("lang/en.yml", false);
            plugin.saveResource("lang/de.yml", false);
        }

        // Load all language files from the directory
        File[] langFiles = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File file : langFiles) {
                String langCode = file.getName().replace(".yml", "");
                FileConfiguration langConfig = YamlConfiguration.loadConfiguration(file);

                // Check for updates in the default file
                InputStream defaultLangStream = plugin.getResource("lang/" + file.getName());
                if (defaultLangStream != null) {
                    FileConfiguration defaultLangConfig = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(defaultLangStream, StandardCharsets.UTF_8));

                    // Add any missing keys from the default file
                    boolean needsUpdate = false;
                    for (String key : defaultLangConfig.getKeys(true)) {
                        if (!langConfig.contains(key)) {
                            langConfig.set(key, defaultLangConfig.get(key));
                            needsUpdate = true;
                        }
                    }

                    // Save the updated configuration if needed
                    if (needsUpdate) {
                        try {
                            langConfig.save(file);
                            plugin.getLogger().info("Updated language file: " + file.getName());
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to update language file: " + file.getName());
                            e.printStackTrace();
                        }
                    }
                }

                // Add to map
                languageFiles.put(langCode, langConfig);
                plugin.getLogger().info("Loaded language: " + langCode);
            }
        }

        // Make sure default language is loaded
        if (!languageFiles.containsKey(defaultLanguage)) {
            plugin.getLogger().warning("Default language '" + defaultLanguage + "' not found. Using English as fallback.");
            // Try to use English as fallback
            if (!languageFiles.containsKey("en")) {
                // Create an empty config as last resort
                languageFiles.put("en", new YamlConfiguration());
                plugin.getLogger().severe("English language file not found! Messages will be empty.");
            }
        }
    }

    public void reloadLanguages() {
        languageFiles.clear();
        loadLanguages();
    }

    public String getMessage(String path) {
        return getMessage(path, defaultLanguage);
    }

    public String getMessage(String path, String lang) {
        FileConfiguration langConfig = languageFiles.getOrDefault(lang, languageFiles.get(defaultLanguage));

        // If the specified language doesn't have this message, try default
        if (langConfig != null && !langConfig.contains(path) && !lang.equals(defaultLanguage)) {
            langConfig = languageFiles.get(defaultLanguage);
        }

        // If still not found, try English
        if ((langConfig == null || !langConfig.contains(path)) && !lang.equals("en") && languageFiles.containsKey("en")) {
            langConfig = languageFiles.get("en");
        }

        // Get the message if available
        if (langConfig != null && langConfig.contains(path)) {
            return langConfig.getString(path, "");
        }

        // Last resort
        return "Missing message: " + path;
    }

    public String getPrefix() {
        return getMessage("prefix", defaultLanguage);
    }
}