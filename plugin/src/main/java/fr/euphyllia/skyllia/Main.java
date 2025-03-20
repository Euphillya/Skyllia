package fr.euphyllia.skyllia;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.utils.Metrics;
import fr.euphyllia.skyllia.api.utils.VersionUtils;
import fr.euphyllia.skyllia.commands.admin.SkylliaAdminCommand;
import fr.euphyllia.skyllia.commands.admin.SubAdminCommandImpl;
import fr.euphyllia.skyllia.commands.common.SkylliaCommand;
import fr.euphyllia.skyllia.commands.common.SubCommandImpl;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
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
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class Main extends JavaPlugin {

    private final Logger logger = LogManager.getLogger(this);
    private InterneAPI interneAPI;
    private SubCommandRegistry commandRegistry;
    private SubCommandRegistry adminCommandRegistry;

    @Override
    public void onEnable() {
        if (!initializeInterneAPI()) {
            return;
        }

        if (!loadConfigurations()) {
            return;
        }

        initializeCommandsDispatcher();

        initializeManagers();
        registerListeners();
        scheduleCacheUpdate();
        checkDisabledConfig();

        new Metrics(this, 20874);
    }

    @Override
    public void onDisable() {
        Bukkit.getAsyncScheduler().cancelTasks(this);
        Bukkit.getGlobalRegionScheduler().cancelTasks(this);
        if (this.interneAPI != null && this.interneAPI.getDatabaseLoader() != null) {
            this.interneAPI.getDatabaseLoader().closeDatabase();
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

    private boolean initializeInterneAPI() {
        try {
            this.interneAPI = new InterneAPI(this);
            this.interneAPI.loadAPI();
            return true;
        } catch (UnsupportedMinecraftVersionException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
    }

    private boolean loadConfigurations() {
        try {
            this.interneAPI.setupFirstSchematic(getDataFolder(), getResource("schematics/default.schem"));

            ConfigLoader.init(getDataFolder());

            if (!this.interneAPI.setupConfigs(getDataFolder(), "language.toml", LanguageToml::init) ||
                    !this.interneAPI.setupConfigs(getDataFolder(), "permissions.toml", PermissionsToml::init)) {
                Bukkit.getPluginManager().disablePlugin(this);
                return false;
            }

            if (!this.interneAPI.setupSGBD()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return false;
            }

            return true;
        } catch (DatabaseException | IOException exception) {
            logger.log(Level.FATAL, exception, exception);
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
    }

    private void initializeManagers() {
        this.interneAPI.setManagers(new Managers(interneAPI));
        this.interneAPI.getManagers().init();
        this.commandRegistry = new SubCommandImpl();
        this.adminCommandRegistry = new SubAdminCommandImpl();
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        // Bukkit Events
        registerEvent(pluginManager, new JoinEvent(this.interneAPI));
        registerEvent(pluginManager, new BlockEvent(this.interneAPI));
        registerEvent(pluginManager, new InventoryEvent(this.interneAPI));
        registerEvent(pluginManager, new PlayerEvent(this.interneAPI));
        registerEvent(pluginManager, new DamageEvent(this.interneAPI));
        registerEvent(pluginManager, new InteractEvent(this.interneAPI));
        registerEvent(pluginManager, new TeleportEvent(this.interneAPI)); // TODO: Doesn't work with Folia 1.19.4-1.21.4
        registerEvent(pluginManager, new PistonEvent(this.interneAPI));

        if (VersionUtils.IS_FOLIA) {
            registerEvent(pluginManager, new PortalAlternativeFoliaEvent(this.interneAPI));
        }
        if (VersionUtils.IS_PAPER) {
            registerEvent(pluginManager, new PortalAlternativePaperEvent());
        }

        // GameRule Events
        registerEvent(pluginManager, new BlockGameRuleEvent(this.interneAPI));
        registerEvent(pluginManager, new ExplosionEvent(this.interneAPI));
        registerEvent(pluginManager, new GriefingEvent(this.interneAPI));
        registerEvent(pluginManager, new MobSpawnEvent(this.interneAPI));
        registerEvent(pluginManager, new PickupEvent(this.interneAPI));

        // Skyblock Event
        registerEvent(pluginManager, new SkyblockEvent(this.interneAPI));
    }

    private void registerEvent(PluginManager pluginManager, Object listener) {
        pluginManager.registerEvents((org.bukkit.event.Listener) listener, this);
    }

    private void scheduleCacheUpdate() {
        Runnable cacheUpdateTask = () -> Bukkit.getOnlinePlayers().forEach(player -> this.interneAPI.updateCache(player));

        Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> cacheUpdateTask.run(), 1, ConfigLoader.general.getUpdateCacheTimer(), TimeUnit.SECONDS);
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

    private void initializeCommandsDispatcher() {
        LifecycleEventManager<@NotNull Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("skyllia", "Islands commands", List.of("is", "ob"), new SkylliaCommand(this));
            commands.register("skylliadmin", "Administrator commands", List.of("isadmin", "skylliaadmin"), new SkylliaAdminCommand(this));
        });
    }
}
