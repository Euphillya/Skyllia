package fr.euphyllia.skyllia.listeners.permissions.player;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.plugin.Plugin;

public class ItemPickupPermissions implements PermissionModule {

    private PermissionId ITEM_PICKUP;

    @EventHandler(ignoreCancelled = true)
    public void onPickup(final EntityPickupItemEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;

        final Location location = player.getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        final boolean hasBypass = player.hasPermission("skyllia.player.item.pickup.bypass");
        final boolean hasPermission = hasBypass || SkylliaAPI.getPermissionsManager().hasPermission(player, island, ITEM_PICKUP, null, ConfigLoader.general.isDebugPermission());
        if (!hasPermission) {
            event.setCancelled(true);
            return;
        }
        if (!hasBypass && ListenersUtils.isBlockOutsideIsland(island, location, event)) {
            return;
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ITEM_PICKUP = registry.register(new PermissionNode(
                new NamespacedKey(owner, "item.pickup"),
                "island.permission.item_pickup.name",
                "island.permission.item_pickup.description"
        ));
    }
}
