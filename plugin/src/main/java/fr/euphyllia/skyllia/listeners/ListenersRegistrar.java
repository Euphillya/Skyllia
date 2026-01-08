package fr.euphyllia.skyllia.listeners;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.utils.VersionUtils;
import fr.euphyllia.skyllia.listeners.bukkitevents.blocks.PistonEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.folia.PortalAlternativeFoliaEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.paper.PortalAlternativePaperEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.player.JoinEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.player.MoveEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.player.TeleportEvent;
import fr.euphyllia.skyllia.listeners.permissions.block.*;
import fr.euphyllia.skyllia.listeners.permissions.decor.DecorHangingBreakPermissions;
import fr.euphyllia.skyllia.listeners.permissions.decor.DecorHangingPlacePermissions;
import fr.euphyllia.skyllia.listeners.permissions.entity.EntityBreedPermissions;
import fr.euphyllia.skyllia.listeners.permissions.entity.EntityDamagePermissions;
import fr.euphyllia.skyllia.listeners.permissions.entity.EntityInteractPermissions;
import fr.euphyllia.skyllia.listeners.permissions.inventory.InventoryModifyClickPermissions;
import fr.euphyllia.skyllia.listeners.permissions.inventory.InventoryModifyDragPermissions;
import fr.euphyllia.skyllia.listeners.permissions.inventory.InventoryOpenPermissions;
import fr.euphyllia.skyllia.listeners.permissions.island.*;
import fr.euphyllia.skyllia.listeners.permissions.island.flags.*;
import fr.euphyllia.skyllia.listeners.permissions.player.ItemDropPermissions;
import fr.euphyllia.skyllia.listeners.permissions.player.ItemPickupPermissions;
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
        registerEvent(pluginManager, new TeleportEvent(interneAPI));
        registerEvent(pluginManager, new PistonEvent(interneAPI));
        registerEvent(pluginManager, new MoveEvent());

        // Folia/Paper specifics
        if (VersionUtils.IS_FOLIA) {
            registerEvent(pluginManager, new PortalAlternativeFoliaEvent(interneAPI));
        }
        if (VersionUtils.IS_PAPER) {
            registerEvent(pluginManager, new PortalAlternativePaperEvent());
        }

        // Skyblock Events
        registerEvent(pluginManager, new SkyblockEvent(interneAPI));

        // Permissions Listeners
        var moduleManager = SkylliaAPI.getPermissionModuleManager();
        moduleManager.addModule(plugin, new BlockBreakPermissions());
        moduleManager.addModule(plugin, new BlockInteractPermissions());
        moduleManager.addModule(plugin, new BlockPhysicalPermissions());
        moduleManager.addModule(plugin, new BlockPlacePermissions());
        moduleManager.addModule(plugin, new BlockUseBucketPermissions());
        moduleManager.addModule(plugin, new DecorHangingBreakPermissions());
        moduleManager.addModule(plugin, new DecorHangingPlacePermissions());
        moduleManager.addModule(plugin, new EntityBreedPermissions());
        moduleManager.addModule(plugin, new EntityDamagePermissions());
        moduleManager.addModule(plugin, new EntityInteractPermissions());
        moduleManager.addModule(plugin, new InventoryModifyClickPermissions());
        moduleManager.addModule(plugin, new InventoryModifyDragPermissions());
        moduleManager.addModule(plugin, new InventoryOpenPermissions());
        moduleManager.addModule(plugin, new ItemPickupPermissions());
        moduleManager.addModule(plugin, new ItemDropPermissions());

        // Permissions flags island
        moduleManager.addModule(plugin, new IslandCreeperGriefFlag());
        moduleManager.addModule(plugin, new IslandEndermanGriefFlag());
        moduleManager.addModule(plugin, new IslandGhastGriefFlag());
        moduleManager.addModule(plugin, new IslandTntGriefFlag());
        moduleManager.addModule(plugin, new IslandWitherGriefFlag());
        moduleManager.addModule(plugin, new IslandWitherSkullGriefFlag());
        moduleManager.addModule(plugin, new IslandAllowEndermanGriefPermissions());
        moduleManager.addModule(plugin, new IslandAllowExplosionsBlockPermissions());
        moduleManager.addModule(plugin, new IslandAllowExplosionsEntityPermissions());
        moduleManager.addModule(plugin, new IslandAllowFireBurnPermissions());
        moduleManager.addModule(plugin, new IslandAllowFireIgnitePermissions());
        moduleManager.addModule(plugin, new IslandAllowFireSpreadPermissions());
        moduleManager.addModule(plugin, new IslandAllowFluidsPermissions());
        moduleManager.addModule(plugin, new IslandAllowPistonsExtendPermissions());
        moduleManager.addModule(plugin, new IslandAllowPistonsRetractPermissions());

        moduleManager.initAndRegisterAll();
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