package fr.euphyllia.skyllia.listeners;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.listeners.bukkitevents.blocks.PistonEvent;
import fr.euphyllia.skyllia.listeners.bukkitevents.blocks.GrowEvent;
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
import fr.euphyllia.skyllia.listeners.permissions.flags.entity.*;
import fr.euphyllia.skyllia.listeners.permissions.flags.explosion.IslandAllowExplosionsBlockPermissions;
import fr.euphyllia.skyllia.listeners.permissions.flags.explosion.IslandAllowExplosionsEntityPermissions;
import fr.euphyllia.skyllia.listeners.permissions.flags.fire.IslandAllowFireBurnPermissions;
import fr.euphyllia.skyllia.listeners.permissions.flags.fire.IslandAllowFireIgnitePermissions;
import fr.euphyllia.skyllia.listeners.permissions.flags.fire.IslandAllowFireSpreadPermissions;
import fr.euphyllia.skyllia.listeners.permissions.flags.grief.*;
import fr.euphyllia.skyllia.listeners.permissions.flags.other.IslandAllowFluidsPermissions;
import fr.euphyllia.skyllia.listeners.permissions.flags.redstone.IslandAllowPistonsExtendPermissions;
import fr.euphyllia.skyllia.listeners.permissions.flags.redstone.IslandAllowPistonsRetractPermissions;
import fr.euphyllia.skyllia.listeners.permissions.inventory.InventoryModifyClickPermissions;
import fr.euphyllia.skyllia.listeners.permissions.inventory.InventoryModifyDragPermissions;
import fr.euphyllia.skyllia.listeners.permissions.inventory.InventoryOpenPermissions;
import fr.euphyllia.skyllia.listeners.permissions.player.ItemDropPermissions;
import fr.euphyllia.skyllia.listeners.permissions.player.ItemPickupPermissions;
import fr.euphyllia.skyllia.listeners.permissions.player.TeleportPermissions;
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
        registerEvent(pluginManager, new GrowEvent(interneAPI));
        registerEvent(pluginManager, new MoveEvent());

        // Folia/Paper specifics
        if (SkylliaAPI.isFolia()) {
            registerEvent(pluginManager, new PortalAlternativeFoliaEvent(interneAPI));
        }
        registerEvent(pluginManager, new PortalAlternativePaperEvent());

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
        moduleManager.addModule(plugin, new TeleportPermissions());

        // Permissions flags island
        var flagModuleManager = SkylliaAPI.getFlagModuleManager();
        flagModuleManager.addModule(plugin, new IslandCreeperGriefFlag());
        flagModuleManager.addModule(plugin, new IslandEndermanGriefFlag());
        flagModuleManager.addModule(plugin, new IslandGhastGriefFlag());
        flagModuleManager.addModule(plugin, new IslandTntGriefFlag());
        flagModuleManager.addModule(plugin, new IslandWitherGriefFlag());
        flagModuleManager.addModule(plugin, new IslandWitherSkullGriefFlag());
        flagModuleManager.addModule(plugin, new IslandAllowEndermanGriefPermissions());
        flagModuleManager.addModule(plugin, new IslandAllowExplosionsBlockPermissions());
        flagModuleManager.addModule(plugin, new IslandAllowExplosionsEntityPermissions());
        flagModuleManager.addModule(plugin, new IslandAllowFireBurnPermissions());
        flagModuleManager.addModule(plugin, new IslandAllowFireIgnitePermissions());
        flagModuleManager.addModule(plugin, new IslandAllowFireSpreadPermissions());
        flagModuleManager.addModule(plugin, new IslandAllowFluidsPermissions());
        flagModuleManager.addModule(plugin, new IslandAllowPistonsExtendPermissions());
        flagModuleManager.addModule(plugin, new IslandAllowPistonsRetractPermissions());
        flagModuleManager.addModule(plugin, new IslandMobSpawnBossFlag());
        flagModuleManager.addModule(plugin, new IslandMobSpawnHostileAdjacentFlag());
        flagModuleManager.addModule(plugin, new IslandMobSpawnHostileFlag());
        flagModuleManager.addModule(plugin, new IslandMobSpawnNeutralFlag());
        flagModuleManager.addModule(plugin, new IslandMobSpawnOtherFlag());
        flagModuleManager.addModule(plugin, new IslandMobSpawnPassiveFlag());

        moduleManager.initAndRegisterAll();
        flagModuleManager.initAndRegisterAll();
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