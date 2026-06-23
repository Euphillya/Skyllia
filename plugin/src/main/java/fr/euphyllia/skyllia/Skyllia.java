package fr.euphyllia.skyllia;

import dev.faststats.ErrorTracker;
import dev.faststats.bukkit.BukkitContext;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.utils.metrics.BStatsMetrics;
import fr.euphyllia.skyllia.commands.CommandRegistrar;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.hook.HookBootstrap;
import fr.euphyllia.skyllia.listeners.ListenersRegistrar;
import fr.euphyllia.skyllia.papi.SkylliaExpansion;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.utils.UpdateCheckerTask;
import net.md_5.bungee.api.ChatColor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class Skyllia extends JavaPlugin {

    public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware();
    private static Skyllia instance;
    private final Logger logger = LogManager.getLogger(this);
    private final BukkitContext context = new BukkitContext.Factory(this, "f329c2c0e1c9562dffebed9b6786d4c9")
            .errorTrackerService(ERROR_TRACKER)
            .create();
    private InterneAPI interneAPI;
    private SubCommandRegistry commandRegistry;
    private SubCommandRegistry adminCommandRegistry;
    private BStatsMetrics bStatsMetrics;

    public static Skyllia getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        context.ready();
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
            logger.error("Incompatible Minecraft version detected. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if (!loadConfigurations()) {
            logger.error("Configuration loading failed.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize managers
        this.interneAPI.setManagers(new fr.euphyllia.skyllia.managers.Managers(interneAPI));
        this.interneAPI.getManagers().init();

        ConfigLoader.reloadConfigs();

        this.interneAPI.initWorldModifier();

        // Register commands via CommandRegistrar
        CommandRegistrar commandRegistrar = new CommandRegistrar(this);
        commandRegistrar.registerCommands();

        this.commandRegistry = commandRegistrar.getCommandRegistry();
        this.adminCommandRegistry = commandRegistrar.getAdminCommandRegistry();

        // Register listeners
        new ListenersRegistrar(this, interneAPI).registerListeners();

        HookBootstrap.registerAll();

        bStatsMetrics = new BStatsMetrics(this, 20874);

        ConfigLoader.permissionsV2.compileNow();
        ConfigLoader.islandFlags.compileNow();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SkylliaExpansion(this).register();
        }

        if (ConfigLoader.general.getUpdateCheckerSettings().enabled()) {
            UpdateCheckerTask.start(this);
        }
    }

    @Override
    public void onDisable() {
        context.shutdown();
        if (bStatsMetrics != null) {
            bStatsMetrics.shutdown();
        }

        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);

        HookBootstrap.unregisterAll();

        if (this.interneAPI != null) {
            var worldModifier = this.interneAPI.getWorldModifier();
            if (worldModifier != null) {
                worldModifier.shutdown();
            }
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

            boolean ok = this.interneAPI.setupSGBD();
            if (!ok) {
                logger.error("[SGBD] setupSGBD() returned false (no exception). Check DB config/credentials/driver/reachability.");
            }
            return ok;
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

    private void printStartupBanner() {
        final String VIOLET = "§d";
        final String GRAY = "§7";
        final String WHITE = "§f";
        final String SEP = VIOLET + "━".repeat(50);

        String[][] info = {
                {"Version", getPluginMeta().getVersion()},
                {"Server", Bukkit.getName() + WHITE + " (" + VIOLET + Bukkit.getVersion() + WHITE + ")"},
                {"Thread Model", SkylliaAPI.isFolia() ? "Multi" : "Single"},
                {"CPU Cores", String.valueOf(Runtime.getRuntime().availableProcessors())},
        };

        List<String> lines = new ArrayList<>();
        lines.add(SEP);
        lines.add(VIOLET + center(getPluginMeta().getName() + " - " + getPluginMeta().getDescription(), 50));
        lines.add(SEP);
        for (String[] entry : info)
            lines.add(GRAY + " » " + WHITE + entry[0] + ": " + VIOLET + entry[1]);
        lines.add(SEP);

        ConsoleCommandSender console = Bukkit.getConsoleSender();
        lines.forEach(console::sendMessage);
    }

    private String center(String text, int lineWidth) {
        int textLength = ChatColor.stripColor(text).length();
        int padding = (lineWidth - textLength) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }
}
