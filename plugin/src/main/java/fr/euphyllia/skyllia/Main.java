package fr.euphyllia.skyllia;


import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.exceptions.DatabaseException;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.commands.admin.SkylliaAdminCommand;
import fr.euphyllia.skyllia.commands.common.SkylliaCommand;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.listeners.bukkitevents.BlockEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.PortailAlternativeFoliaEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.entity.DamageEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.BlockGameRuleEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity.ExplosionEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity.GriefingEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity.MobSpawnEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity.PickupEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.player.*;
import fr.euphyllia.skyllia.listeners.skyblockevents.SkyblockEvent;
import fr.euphyllia.skyllia.managers.Managers;
import fr.euphyllia.skyllia.utils.Metrics;
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
        this.setupAdminCommands();
        this.loadListener();
        this.runCache();
        this.disabledConfig();
        this.interneAPI.loadAPI();
        new Metrics(this, 20874);
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

    private void setupAdminCommands() {
        SkylliaAdminCommand sc = new SkylliaAdminCommand(this);
        PluginCommand command = getServer().getPluginCommand("skylliadmin");
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
        pluginManager.registerEvents(new DamageEvent(this.interneAPI), this);
        pluginManager.registerEvents(new InteractEvent(this.interneAPI), this);
        pluginManager.registerEvents(new TeleportEvent(this.interneAPI), this); // Todo Don't work with folia 1.19.4-1.20.2
        if (this.interneAPI.isFolia()) {
            pluginManager.registerEvents(new PortailAlternativeFoliaEvent(this.interneAPI), this);
        }
        // GameRule Events
        pluginManager.registerEvents(new BlockGameRuleEvent(this.interneAPI), this);
        pluginManager.registerEvents(new ExplosionEvent(this.interneAPI), this);
        pluginManager.registerEvents(new GriefingEvent(this.interneAPI), this);
        pluginManager.registerEvents(new MobSpawnEvent(this.interneAPI), this);
        pluginManager.registerEvents(new PickupEvent(this.interneAPI), this);

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