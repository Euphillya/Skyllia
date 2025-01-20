package fr.euphyllia.skyllia;

import fr.euphyllia.skyllia.addons.AddonLoader;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.addons.AddonLoadPhase;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.utils.Metrics;
import fr.euphyllia.skyllia.cache.CacheScheduler;
import fr.euphyllia.skyllia.commands.CommandRegistrar;
import fr.euphyllia.skyllia.configuration.ConfigManager;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.listeners.ListenersRegistrar;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class of the plugin, coordinating initialization, enabling, and disabling.
 */
public class Main extends JavaPlugin {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private InterneAPI interneAPI;

    // Si vous souhaitez garder un accès aux registries
    private SubCommandRegistry commandRegistry;
    private SubCommandRegistry adminCommandRegistry;

    @Override
    public void onLoad() {
        // Load addons which must be loaded before the plugin is fully enabled
        new AddonLoader(this, LOGGER).loadAddons(AddonLoadPhase.BEFORE);
    }

    @Override
    public void onEnable() {
        // Initialize the internal API (checks server version compatibility)
        if (!initializeInterneAPI()) {
            return; // Stop if version is not supported
        }

        // Load and check all configurations (config.toml, language.toml, permissions.toml)
        ConfigManager configManager = new ConfigManager(this, LOGGER);
        if (!configManager.loadConfigurations(interneAPI)) {
            // If config load fails, the plugin is disabled (inside loadConfigurations)
            return;
        }

        // Register commands via CommandRegistrar
        CommandRegistrar commandRegistrar = new CommandRegistrar(this);
        commandRegistrar.registerCommands();
        // Récupérer les SubCommandRegistry si besoin
        this.commandRegistry = commandRegistrar.getCommandRegistry();
        this.adminCommandRegistry = commandRegistrar.getAdminCommandRegistry();

        // Initialize managers
        this.interneAPI.setManagers(new fr.euphyllia.skyllia.managers.Managers(interneAPI));
        this.interneAPI.getManagers().init();

        // Register listeners
        new ListenersRegistrar(this, interneAPI, LOGGER).registerListeners();

        // Schedule cache updates
        new CacheScheduler(this, interneAPI, LOGGER).scheduleCacheUpdate();

        // Check server configs for Nether/End warnings
        checkDisabledConfig();

        // Load addons which must be loaded after the plugin is fully enabled
        new AddonLoader(this, LOGGER).loadAddons(AddonLoadPhase.AFTER);

        // bStats metrics
        new Metrics(this, 20874);
    }

    @Override
    public void onDisable() {
        // Disable all loaded addons
        AddonLoader.disableAllAddons();

        // Cancel scheduled tasks
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);

        // Close DB if needed
        if (this.interneAPI != null && this.interneAPI.getDatabaseLoader() != null) {
            this.interneAPI.getDatabaseLoader().closeDatabase();
        }
    }

    /**
     * Initializes the internal API, handling version compatibility.
     *
     * @return true if initialization succeeded, false otherwise
     */
    private boolean initializeInterneAPI() {
        try {
            this.interneAPI = new InterneAPI(this);
            return true;
        } catch (UnsupportedMinecraftVersionException e) {
            LOGGER.log(Level.FATAL, e.getMessage(), e);
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
    }

    /**
     * Checks if Nether/End are disabled, and logs a warning if they are not (based on config).
     */
    private void checkDisabledConfig() {
        if (fr.euphyllia.skyllia.api.utils.VersionUtils.IS_FOLIA && !ConfigToml.suppressWarningNetherEndEnabled) {
            if (Bukkit.getAllowNether()) {
                LOGGER.log(Level.WARN, "Disable nether in server.properties to disable nether portals!");
            }
            if (Bukkit.getAllowEnd()) {
                LOGGER.log(Level.WARN, "Disable end in bukkit.yml to disable end portals!");
            }
        }
    }


    public InterneAPI getInterneAPI() {
        return this.interneAPI;
    }

    public SubCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public SubCommandRegistry getAdminCommandRegistry() {
        return adminCommandRegistry;
    }
}
