package fr.euphyllia.skyllia.configuration;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

import java.io.IOException;

/**
 * Manages loading of various plugin configurations (config.toml, language.toml, permissions.toml).
 */
public class ConfigManager {

    private final Main plugin;
    private final Logger logger;

    /**
     * Constructs a ConfigManager.
     *
     * @param plugin the main plugin instance
     * @param logger a shared logger
     */
    public ConfigManager(Main plugin, Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    /**
     * Loads all necessary configurations using the provided InterneAPI.
     *
     * @param interneAPI the internal API
     * @return true if successful, false otherwise
     */
    public boolean loadConfigurations(InterneAPI interneAPI) {
        try {
            // Copie la schematic par défaut
            interneAPI.setupFirstSchematic(plugin.getDataFolder(), plugin.getResource("schematics/default.schem"));

            // Load config.toml
            if (!interneAPI.setupConfigs(plugin.getDataFolder(), "config.toml", ConfigToml::init)) {
                Bukkit.getPluginManager().disablePlugin(plugin);
                return false;
            }

            // Load language.toml
            if (!interneAPI.setupConfigs(plugin.getDataFolder(), "language.toml", LanguageToml::init)) {
                Bukkit.getPluginManager().disablePlugin(plugin);
                return false;
            }

            // Load permissions.toml
            if (!interneAPI.setupConfigs(plugin.getDataFolder(), "permissions.toml", PermissionsToml::init)) {
                Bukkit.getPluginManager().disablePlugin(plugin);
                return false;
            }

            // Initialize the DB
            if (!interneAPI.setupSGBD()) {
                Bukkit.getPluginManager().disablePlugin(plugin);
                return false;
            }
        } catch (DatabaseException | IOException e) {
            logger.log(Level.FATAL, "Error loading configurations or initializing DB:", e);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return false;
        }
        return true;
    }
}
