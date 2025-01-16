package fr.euphyllia.skyllia;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.addons.SkylliaAddon;
import fr.euphyllia.skyllia.api.commands.SubCommandRegistry;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.utils.Metrics;
import fr.euphyllia.skyllia.api.utils.VersionUtils;
import fr.euphyllia.skyllia.commands.admin.SkylliaAdminCommand;
import fr.euphyllia.skyllia.commands.admin.SubAdminCommandImpl;
import fr.euphyllia.skyllia.commands.common.SkylliaCommand;
import fr.euphyllia.skyllia.commands.common.SubCommandImpl;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

public class Main extends JavaPlugin {

    private final Logger logger = LogManager.getLogger(this);
    private final List<SkylliaAddon> addons = new ArrayList<>();
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

        loadAddons();

        new Metrics(this, 20874);
    }

    @Override
    public void onDisable() {
        for (SkylliaAddon addon : addons) {
            try {
                addon.onDisabled();
            } catch (Exception exception) {
                logger.log(Level.ERROR, "Error disabling addon: {}", addon.getClass().getName(), exception);
            }
        }
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
            if (!this.interneAPI.setupConfigs(getDataFolder(), "config.toml", ConfigToml::init) ||
                    !this.interneAPI.setupConfigs(getDataFolder(), "language.toml", LanguageToml::init) ||
                    !this.interneAPI.setupConfigs(getDataFolder(), "permissions.toml", PermissionsToml::init) ||
                    !this.interneAPI.setupSGBD()) {
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

        Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> cacheUpdateTask.run(), 1, ConfigToml.updateCacheTimer, TimeUnit.SECONDS);
    }

    private void checkDisabledConfig() {
        /* Since 1.20.3, there is a gamerule that allows you to increase the number of ticks between entering a portal and teleporting.
          This makes the configuration possibly useless.
          BUT just in case, I leave the message enabled by default.
         */
        if (VersionUtils.IS_FOLIA && !ConfigToml.suppressWarningNetherEndEnabled) {
            if (Bukkit.getAllowNether()) {
                logger.log(Level.WARN, "Disable nether in server.properties to disable nether portals!");
            }
            if (Bukkit.getAllowEnd()) {
                logger.log(Level.WARN, "Disable end in bukkit.yml to disable end portals!");
            }
        }
    }

    private void initializeCommandsDispatcher() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("skyllia", "Islands commands", List.of("is"), new SkylliaCommand(this));
            commands.register("skylliaadmin", "Administrator commands", List.of("isadmin"), new SkylliaAdminCommand(this));
        });
    }


    private void loadAddons() {
        File extensionsDir = new File(getDataFolder(), "addons");
        if (!extensionsDir.exists()) {
            extensionsDir.mkdirs();
            logger.info("Addon directory created at {}", extensionsDir.getAbsolutePath());
            return;
        }

        File[] files = extensionsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (files == null) {
            logger.warn("No addon files found in {}", extensionsDir.getAbsolutePath());
            return;
        }

        for (File file : files) {
            try {
                URL jarUrl = file.toURI().toURL();
                URLClassLoader classLoader = new URLClassLoader(new URL[]{jarUrl}, this.getClass().getClassLoader());

                ServiceLoader<SkylliaAddon> serviceLoader = ServiceLoader.load(SkylliaAddon.class, classLoader);
                for (SkylliaAddon addon : serviceLoader) {
                    addon.onLoad(this);
                    addon.onEnable();
                    addons.add(addon);
                    logger.info("Addon loaded: {}", addon.getClass().getName());
                }
            } catch (Exception e) {
                logger.log(Level.ERROR, "Failed to load addon: {}", file.getName(), e);
            }
        }
    }
}
