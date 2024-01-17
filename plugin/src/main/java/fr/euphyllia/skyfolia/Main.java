package fr.euphyllia.skyfolia;


import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.exceptions.DatabaseException;
import fr.euphyllia.skyfolia.commands.SkyFoliaCommand;
import fr.euphyllia.skyfolia.listeners.bukkitevents.BlockEvent;
import fr.euphyllia.skyfolia.listeners.bukkitevents.InventoryEvent;
import fr.euphyllia.skyfolia.listeners.bukkitevents.JoinEvent;
import fr.euphyllia.skyfolia.listeners.bukkitevents.PlayerEvent;
import fr.euphyllia.skyfolia.listeners.skyblockevents.SkyblockEvent;
import fr.euphyllia.skyfolia.managers.Managers;
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
        this.interneAPI = new InterneAPI(this);
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
            this.logger.log(Level.FATAL, exception);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.interneAPI.setManagers(new Managers(interneAPI));
        this.interneAPI.getManagers().init();
        this.setupCommands();
        this.loadListener();
        this.runCache();
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
        SkyFoliaCommand sc = new SkyFoliaCommand(this);
        PluginCommand command = getServer().getPluginCommand("skyfolia");
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

        // Skyblock Event
        pluginManager.registerEvents(new SkyblockEvent(this.interneAPI), this);
    }

    private void runCache() {
        ScheduledExecutorService executors = Executors.newScheduledThreadPool(2);
        executors.scheduleAtFixedRate(() -> {
            logger.log(Level.FATAL, "Update en cours");
            Bukkit.getOnlinePlayers().forEach(player -> this.interneAPI.updateCache(player));
        }, 0, 10, TimeUnit.SECONDS);

    }
}