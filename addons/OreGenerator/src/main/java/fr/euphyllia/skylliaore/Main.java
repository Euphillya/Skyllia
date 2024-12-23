package fr.euphyllia.skylliaore;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skylliaore.commands.OreCommands;
import fr.euphyllia.skylliaore.config.DefaultConfig;
import fr.euphyllia.skylliaore.database.MariaDBInit;
import fr.euphyllia.skylliaore.listeners.OreEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin {

    private static final Logger logger = LogManager.getLogger(Main.class);
    private static DefaultConfig config;
    private static Cache cache;
    private static boolean oraxenLoaded = false;

    @NotNull
    public static DefaultConfig getDefaultConfig() {
        return config;
    }

    public static Cache getCache() {
        return cache;
    }

    public static boolean isOraxenLoaded() {
        return oraxenLoaded;
    }

    @Override
    public void onEnable() {
        oraxenLoaded = Bukkit.getPluginManager().isPluginEnabled("Oraxen");
        // Plugin startup logic
        initializeConfig();
        initializeDatabase();
        initializeCache();
        registerEvents();
        registerCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initializeConfig() {
        saveDefaultConfig();
        config = new DefaultConfig();
        config.loadConfiguration(getConfig());
    }

    private void initializeDatabase() {
        MariaDBInit mariaDBInit = new MariaDBInit();
        try {
            mariaDBInit.init();
        } catch (DatabaseException e) {
            logger.error("Database initialization failed", e);
            throw new RuntimeException(e);
        }
    }

    private void initializeCache() {
        cache = new Cache();
    }

    private void registerEvents() {
        getServer().getPluginManager().registerEvents(new OreEvent(), this);
    }

    private void registerCommands() {
        SkylliaAPI.registerAdminCommands(new OreCommands(), "generator");
    }
}
