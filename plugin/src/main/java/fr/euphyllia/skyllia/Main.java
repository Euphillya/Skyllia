package fr.euphyllia.skyllia;

import fr.euphyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.utils.VersionUtils;
import fr.euphyllia.skyllia.commands.SkylliaCommandInterface;
import fr.euphyllia.skyllia.commands.admin.SkylliaAdminCommand;
import fr.euphyllia.skyllia.commands.common.SkylliaCommand;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.configuration.PermissionsToml;
import fr.euphyllia.skyllia.listeners.bukkitevents.blocks.BlockEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.blocks.PistonEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.entity.DamageEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.folia.PortalAlternativeFoliaEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.BlockGameRuleEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity.ExplosionEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity.GriefingEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity.MobSpawnEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.gamerule.entity.PickupEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.paper.PortalAlternativePaperEvent;
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
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {

    private final Logger logger = LogManager.getLogger(this);
    private InterneAPI interneAPI;

    @Override
    public void onEnable() {
        try {
            this.interneAPI = new InterneAPI(this);
        } catch (UnsupportedMinecraftVersionException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        this.interneAPI.loadAPI();
        try {
            this.interneAPI.setupFirstSchematic(getDataFolder(), getResource("schematics/default.schem"));
            if (!this.interneAPI.setupConfigs(this.getDataFolder(), "config.toml", ConfigToml::init)) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            if (!this.interneAPI.setupConfigs(this.getDataFolder(), "language.toml", LanguageToml::init)) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            if (!this.interneAPI.setupConfigs(this.getDataFolder(), "permissions.toml", PermissionsToml::init)) {
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
        this.setupCommands("skyllia", new SkylliaCommand(this));
        this.setupCommands("skylliadmin", new SkylliaAdminCommand(this));
        this.loadListener();
        this.runCache();
        this.disabledConfig();

        new Metrics(this.interneAPI, 20874);
    }

    @Override
    public void onDisable() {
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
        if (this.interneAPI.getDatabaseLoader() != null) {
            this.interneAPI.getDatabaseLoader().closeDatabase();
        }
    }

    public InterneAPI getInterneAPI() {
        return this.interneAPI;
    }

    private void setupCommands(String commands, SkylliaCommandInterface sc) {
        PluginCommand command = getServer().getPluginCommand(commands);
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
        pluginManager.registerEvents(new TeleportEvent(this.interneAPI), this); // Todo Don't work with folia 1.19.4-1.20.6 (can work on Bloom, but don't use it)
        pluginManager.registerEvents(new PistonEvent(this.interneAPI), this);
        if (VersionUtils.IS_FOLIA) {
            pluginManager.registerEvents(new PortalAlternativeFoliaEvent(this.interneAPI), this);
        }
        if (VersionUtils.IS_PAPER) {
            pluginManager.registerEvents(new PortalAlternativePaperEvent(), this);
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
        Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> {
            Bukkit.getOnlinePlayers().forEach(player -> this.interneAPI.updateCache(player));
        }, 1, ConfigToml.updateCacheTimer, TimeUnit.SECONDS);
    }

    private void disabledConfig() {
        /* Since 1.20.3, there is a gamerule that allows you to increase the number of ticks between entering a portal and teleporting.
          This makes the configuration possibly useless.
          BUT just in case, I leave the message enabled by default.
         */
        if (VersionUtils.IS_FOLIA && !ConfigToml.suppressWarningNetherEndEnabled) { // D
            if (Bukkit.getAllowNether()) {
                logger.log(Level.WARN, "Disable nether in server.properties to disable nether portals!");
            }
            if (Bukkit.getAllowEnd()) {
                logger.log(Level.WARN, "Disable end in bukkit.yml to disable end portals!");
            }
        }
    }
}