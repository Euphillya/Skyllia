package fr.euphyllia.skyllia.listeners;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.utils.VersionUtils;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

/**
 * Registers all listeners in one place.
 */
public class ListenersRegistrar {

    private final Skyllia plugin;
    private final InterneAPI interneAPI;
    private final Logger logger = LogManager.getLogger(this);

    /**
     * Constructs a ListenersRegistrar.
     *
     * @param plugin     the Skyllia plugin instance
     * @param interneAPI the internal API
     */
    public ListenersRegistrar(Skyllia plugin, InterneAPI interneAPI) {
        this.plugin = plugin;
        this.interneAPI = interneAPI;
    }

    /**
     * Registers all required event listeners.
     */
    public void registerListeners() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();

        // Bukkit Events
        registerEvent(pluginManager, new JoinEvent(interneAPI));
        registerEvent(pluginManager, new BlockEvent(interneAPI));
        registerEvent(pluginManager, new InventoryEvent(interneAPI));
        registerEvent(pluginManager, new PlayerEvent(interneAPI));
        registerEvent(pluginManager, new DamageEvent(interneAPI));
        registerEvent(pluginManager, new InteractEvent(interneAPI));
        registerEvent(pluginManager, new TeleportEvent(interneAPI));
        registerEvent(pluginManager, new PistonEvent(interneAPI));

        // Folia/Paper specifics
        if (VersionUtils.IS_FOLIA) {
            registerEvent(pluginManager, new PortalAlternativeFoliaEvent(interneAPI));
        }
        if (VersionUtils.IS_PAPER) {
            registerEvent(pluginManager, new PortalAlternativePaperEvent());
        }

        // GameRule Events
        registerEvent(pluginManager, new BlockGameRuleEvent(interneAPI));
        registerEvent(pluginManager, new ExplosionEvent(interneAPI));
        registerEvent(pluginManager, new GriefingEvent(interneAPI));
        registerEvent(pluginManager, new MobSpawnEvent(interneAPI));
        registerEvent(pluginManager, new PickupEvent(interneAPI));

        // Skyblock Events
        registerEvent(pluginManager, new SkyblockEvent(interneAPI));
    }

    /**
     * Helper method to register a listener with the plugin's PluginManager.
     *
     * @param pluginManager the PluginManager
     * @param listener      the listener to register
     */
    private void registerEvent(PluginManager pluginManager, Listener listener) {
        pluginManager.registerEvents(listener, plugin);
    }
}