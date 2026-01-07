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
import fr.euphyllia.skyllia.listeners.permissions.player.PlayerDropItemPermissions;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import net.md_5.bungee.api.ChatColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

@SuppressWarnings("UnstableApiUsage")
public class Skyllia extends JavaPlugin {

    private static Skyllia instance;
    private final Logger logger = LogManager.getLogger(this);
    private InterneAPI interneAPI;
    private SubCommandRegistry commandRegistry;
    private SubCommandRegistry adminCommandRegistry;

    public static Skyllia getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        printStartupBanner();
        File oldConfig = new File(getDataFolder(), "config.toml");
        if (oldConfig.exists()) {
            getLogger().severe("══════════════════════════════════════════════════════");
            getLogger().severe("          ❌ OUTDATED CONFIGURATION DETECTED ❌");
            getLogger().severe(" ");
            getLogger().severe("  The file 'config.toml' is no longer supported.");
            getLogger().severe("  Major internal changes have been made.");
            getLogger().severe(" ");
            getLogger().severe("  ⚠️ Please delete or manually migrate your configuration.");
            getLogger().severe("  The plugin will not continue with an outdated config.");
            getLogger().severe("══════════════════════════════════════════════════════");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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

        var permModules = this.interneAPI.getManagers().getPermissionModuleManager();
        permModules.addModule(this, new PlayerDropItemPermissions());

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

        interneAPI.getManagers().getPermissionModuleManager().initAndRegisterAll();
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

    private void printStartupBanner() {
        String pluginName = getPluginMeta().getName();
        String pluginVersion = getPluginMeta().getVersion();
        String description = getPluginMeta().getDescription();
        String serverType = Bukkit.getName(); // ex: Bloom, Paper
        String serverVersion = Bukkit.getVersion();
        String threadModel = VersionUtils.IS_FOLIA ? "Folia (multi-thread)" : "Vanilla (single-thread)";
        int cpuCores = Runtime.getRuntime().availableProcessors();

        String violet = "§d";
        String gray = "§7";
        String white = "§f";
        String separator = violet + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

        Bukkit.getConsoleSender().sendMessage(separator);
        Bukkit.getConsoleSender().sendMessage(violet + center(pluginName + " - " + description, 50));
        Bukkit.getConsoleSender().sendMessage(separator);
        Bukkit.getConsoleSender().sendMessage(gray + " » " + white + "Version: " + violet + pluginVersion);
        Bukkit.getConsoleSender().sendMessage(gray + " » " + white + "Server: " + violet + serverType + white + " (" + violet + serverVersion + white + ")");
        Bukkit.getConsoleSender().sendMessage(gray + " » " + white + "Thread Model: " + violet + threadModel);
        Bukkit.getConsoleSender().sendMessage(gray + " » " + white + "CPU Cores: " + violet + cpuCores);
        Bukkit.getConsoleSender().sendMessage(separator);
    }

    private String center(String text, int lineWidth) {
        int textLength = ChatColor.stripColor(text).length();
        int padding = (lineWidth - textLength) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }
}
