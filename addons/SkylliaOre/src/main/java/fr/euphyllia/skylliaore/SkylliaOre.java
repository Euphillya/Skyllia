package fr.euphyllia.skylliaore;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.api.OreGenerator;
import fr.euphyllia.skylliaore.commands.OreCommands;
import fr.euphyllia.skylliaore.config.DefaultConfig;
import fr.euphyllia.skylliaore.database.mariadb.MariaDBInit;
import fr.euphyllia.skylliaore.database.sqlite.SQLiteOreInit;
import fr.euphyllia.skylliaore.listeners.InfoListener;
import fr.euphyllia.skylliaore.listeners.OreEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class SkylliaOre extends JavaPlugin {

    private static final Logger logger = LogManager.getLogger(SkylliaOre.class);
    private static final ConcurrentHashMap<UUID, Generator> generatorCache = new ConcurrentHashMap<>();
    private static DefaultConfig config;
    private static GeneratorManager generatorManager;
    private static boolean oraxenLoaded = false;
    private static boolean nexoLoaded = false;
    private static SkylliaOre instance;
    private OreGenerator generator;

    @NotNull
    public static DefaultConfig getDefaultConfig() {
        return config;
    }

    public static GeneratorManager getGeneratorManager() {
        return generatorManager;
    }

    public static boolean isOraxenLoaded() {
        return oraxenLoaded;
    }

    public static boolean isNexoLoaded() {
        return nexoLoaded;
    }

    public static SkylliaOre getInstance() {
        return instance;
    }

    public static Generator getCachedGenerator(UUID islandId) {
        return generatorCache.getOrDefault(islandId, getDefaultConfig().getDefaultGenerator());
    }

    public static void updateGeneratorCache(UUID islandId, Generator generator) {
        generatorCache.put(islandId, generator);
    }

    public static void reloadGeneratorCache(UUID islandId) {
        // Recharge depuis la DB de manière asynchrone
        getInstance().getOreGenerator().getGenIsland(islandId).thenAccept(generator ->
                generatorCache.put(islandId, generator)
        );
    }

    public static void clearGeneratorCache() {
        generatorCache.clear();
    }

    private static void startPeriodicGeneratorCacheRefresh() {
        Bukkit.getAsyncScheduler().runAtFixedRate(instance, scheduledTask -> {
            SkylliaAPI.getAllIslandsValid().thenAccept(islands -> {
                for (Island island : islands) {
                    reloadGeneratorCache(island.getId());
                }
            }).exceptionally(e -> {
                logger.error("Error while preloading generator cache", e);
                return null;
            });
        }, 1, 60, TimeUnit.SECONDS);
    }

    @Override
    public void onEnable() {
        instance = this;
        oraxenLoaded = Bukkit.getPluginManager().getPlugin("Oraxen") != null;
        nexoLoaded = Bukkit.getPluginManager().getPlugin("Nexo") != null;
        // Plugin startup logic
        initializeConfig();

        generator = initializeDatabase();
        if (generator == null) return;
        generatorManager = new GeneratorManager(generator);

        getServer().getPluginManager().registerEvents(new OreEvent(), this);
        getServer().getPluginManager().registerEvents(new InfoListener(), this);

        SkylliaAPI.registerAdminCommands(new OreCommands(this), "generator");

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new fr.euphyllia.skylliaore.papi.SkylliaOreExpansion().register();
        }
        startPeriodicGeneratorCacheRefresh();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getAsyncScheduler().cancelTasks(instance);
        Bukkit.getGlobalRegionScheduler().cancelTasks(instance);
        clearGeneratorCache();
    }

    private void initializeConfig() {
        saveDefaultConfig();
        config = new DefaultConfig();
        config.loadConfiguration(getConfig());
    }

    private OreGenerator initializeDatabase() {
        try {
            if (ConfigLoader.database.getMariaDBConfig() != null) {
                MariaDBInit dbInit = new MariaDBInit();
                if (!dbInit.init()) {
                    getLogger().severe("Unable to initialize the MariaDB bank database! The plugin will stop.");
                    getServer().getPluginManager().disablePlugin(this);
                    return null;
                }
                return MariaDBInit.getMariaDbGenerator();
            } else if (ConfigLoader.database.getSqLiteConfig() != null) {
                SQLiteOreInit dbInit = new SQLiteOreInit();
                if (!dbInit.init()) {
                    getLogger().severe("Unable to initialize the SQLite bank database! The plugin will stop.");
                    getServer().getPluginManager().disablePlugin(this);
                    return null;
                }
                return SQLiteOreInit.getSqliteGenerator();
            } else {
                getLogger().severe("No database configuration found! The plugin will stop.");
                getServer().getPluginManager().disablePlugin(this);
                return null;
            }
        } catch (DatabaseException exception) {
            getLogger().severe("Unable to initialize the MariaDB bank database! The plugin will stop.");
            getServer().getPluginManager().disablePlugin(this);
            throw new RuntimeException(exception);
        }
    }

    public OreGenerator getOreGenerator() {
        return generator;
    }
}
