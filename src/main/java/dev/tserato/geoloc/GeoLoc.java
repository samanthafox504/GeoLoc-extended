package dev.tserato.geoloc;

import dev.tserato.geoloc.config.ConfigManager;
import dev.tserato.geoloc.expansion.PAPIExpansion;
import dev.tserato.geoloc.listener.EventListener;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class GeoLoc extends JavaPlugin {

    private EventListener eventListener;
    private ConfigManager configManager;
    private PAPIExpansion papiExpansion;

    @Override
    public void onEnable() {
        // Create and initialize ConfigManager first
        configManager = new ConfigManager(this);

        // Then set up the event listener which depends on ConfigManager
        eventListener = new EventListener(this, configManager);

        new Metrics(this, 21836);

        // Register commands
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(ConfigManager.createGeoLocCommand(configManager, this));
        });

        papiExpansion = new PAPIExpansion(this, configManager);

        papiExpansion.register();

        getLogger().info("GeoLoc plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("GeoLoc plugin has been disabled!");
    }
}