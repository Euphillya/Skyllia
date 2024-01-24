package fr.euphyllia.skyllia;


import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.exceptions.DatabaseException;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.commands.SkylliaCommand;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.listeners.bukkitevents.*;
import fr.euphyllia.skyllia.listeners.skyblockevents.SkyblockEvent;
import fr.euphyllia.skyllia.managers.Managers;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {

    private final Logger logger = LogManager.getLogger(this);
    private InterneAPI interneAPI;

    @Override
    public void onEnable() {
        logger.log(Level.INFO, "Plugin Start");
        try {
            this.interneAPI = new InterneAPI(this);
        } catch (UnsupportedMinecraftVersionException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        try {
            if (!this.interneAPI.setupConfigs(this.getDataFolder(), "config.toml")) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            if (!this.interneAPI.setupConfigLanguage(this.getDataFolder(), "language.toml")) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            if (!this.interneAPI.setupSGBD()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
        } catch (DatabaseException | IOException exception) {
            this.logger.log(Level.FATAL, exception, exception);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.interneAPI.setManagers(new Managers(interneAPI));
        this.interneAPI.getManagers().init();
        this.setupCommands();
        this.loadListener();
        this.runCache();
        this.disabledConfig();
    }

    @Override
    public void onDisable() {
        this.logger.log(Level.INFO, "Plugin Off");
        if (this.interneAPI.getDatabaseLoader() != null) {
            this.interneAPI.getDatabaseLoader().closeDatabase();
        }
    }

    public InterneAPI getInterneAPI() {
        return this.interneAPI;
    }

    private void setupCommands() {
        SkylliaCommand sc = new SkylliaCommand(this);
        PluginCommand command = getServer().getPluginCommand("skyllia");
        if (command == null) {
            logger.log(Level.FATAL, "Command not put in plugin.yml");
            return;
        }
        command.setExecutor(sc);
        command.setTabCompleter(sc);
    }

    private void loadListener() {
        PluginManager pluginManager = getServer().getPluginManager();
        // Bukkit Events
        pluginManager.registerEvents(new JoinEvent(this.interneAPI), this);
        pluginManager.registerEvents(new BlockEvent(this.interneAPI), this);
        pluginManager.registerEvents(new InventoryEvent(this.interneAPI), this);
        pluginManager.registerEvents(new PlayerEvent(this.interneAPI), this);
        if (this.interneAPI.isFolia()) {
            pluginManager.registerEvents(new PortailAlternativeFoliaEvent(this.interneAPI), this);
        }

        // Skyblock Event
        pluginManager.registerEvents(new SkyblockEvent(this.interneAPI), this);
    }

    private void runCache() {
        ScheduledExecutorService executors = Executors.newScheduledThreadPool(2);
        executors.scheduleAtFixedRate(() -> {
            Bukkit.getOnlinePlayers().forEach(player -> this.interneAPI.updateCache(player));
        }, 0, ConfigToml.updateCacheTimer, TimeUnit.SECONDS);
    }

    private void disabledConfig() {
        if (this.getInterneAPI().isFolia()) {
            if (Bukkit.getAllowNether()) {
                logger.log(Level.WARN, "Disable nether in server.properties to disable nether portals!");
            }
            if (Bukkit.getAllowEnd()) {
                logger.log(Level.WARN, "Disable end in bukkit.yml to disable end portals!");
            }
        }
    }
}