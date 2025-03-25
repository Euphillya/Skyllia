package fr.euphyllia.skyllia;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.utils.Metrics;
import fr.euphyllia.skyllia.api.utils.VersionUtils;
import fr.euphyllia.skyllia.cache.CacheScheduler;
import fr.euphyllia.skyllia.commands.CommandRegistrar;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.listeners.ListenersRegistrar;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class Main extends JavaPlugin {

    private final Logger logger = LogManager.getLogger(this);
    private InterneAPI interneAPI;
    private SubCommandRegistry commandRegistry;
    private SubCommandRegistry adminCommandRegistry;

    @Override
    public void onEnable() {
        if (!initializeInterneAPI()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!loadConfigurations()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        this.interneAPI.setManagers(new fr.euphyllia.skyllia.managers.Managers(interneAPI));
        this.interneAPI.getManagers().init();

        // Register commands via CommandRegistrar
        CommandRegistrar commandRegistrar = new CommandRegistrar(this);
        commandRegistrar.registerCommands();

        this.commandRegistry = commandRegistrar.getCommandRegistry();
        this.adminCommandRegistry = commandRegistrar.getAdminCommandRegistry();

        // Register listeners
        new ListenersRegistrar(this, interneAPI).registerListeners();

        // Schedule cache updates
        new CacheScheduler(this, interneAPI).scheduleCacheUpdate();

        checkDisabledConfig();

        new Metrics(this, 20874);
    }

    @Override
    public void onDisable() {
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
        if (this.interneAPI != null) {
            this.interneAPI.getCacheManager().invalidateAll();
            if (this.interneAPI.getDatabaseLoader() != null) {
                this.interneAPI.getDatabaseLoader().closeDatabase();
            }
        }
    }

    public InterneAPI getInterneAPI() {
        return this.interneAPI;
    }

    public @NotNull SubCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public @NotNull SubCommandRegistry getAdminCommandRegistry() {
        return adminCommandRegistry;
    }

    private boolean loadConfigurations() {
        try {
            for (String folder : new String[]{"config", "language", "schematics"}) {
                this.interneAPI.createAndCopyResources(getFile(), folder);
            }

            ConfigLoader.init(getDataFolder());

            return this.interneAPI.setupSGBD();
        } catch (DatabaseException exception) {
            logger.log(Level.FATAL, exception, exception);
            return false;
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
            logger.log(Level.FATAL, e.getMessage(), e);
            return false;
        }
    }


    private void checkDisabledConfig() {
        /* Since 1.20.3, there is a gamerule that allows you to increase the number of ticks between entering a portal and teleporting.
          This makes the configuration possibly useless.
          BUT just in case, I leave the message enabled by default.
         */
        if (VersionUtils.IS_FOLIA && !ConfigLoader.worldManager.isSuppressWarnNetherEndWorld()) {
            if (Bukkit.getAllowNether()) {
                logger.log(Level.WARN, "Disable nether in server.properties to disable nether portals!");
            }
            if (Bukkit.getAllowEnd()) {
                logger.log(Level.WARN, "Disable end in bukkit.yml to disable end portals!");
            }
        }
    }
}
