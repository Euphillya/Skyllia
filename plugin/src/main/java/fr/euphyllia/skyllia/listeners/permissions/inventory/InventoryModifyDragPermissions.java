package fr.euphyllia.skyllia.listeners.permissions.inventory;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.Plugin;

public class InventoryModifyDragPermissions implements PermissionModule {

    private PermissionId INVENTORY_MODIFY;

    @EventHandler(ignoreCancelled = true)
    public void onDrag(final InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        final Location location = player.getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        final boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, INVENTORY_MODIFY, "skyllia.player.inventory.modify.bypass", ConfigLoader.general.isDebugPermission());
        if (!hasPermission) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.INVENTORY_MODIFY = registry.register(new PermissionNode(
                new NamespacedKey(owner, "inventory.modify"),
                "island.permission.inventory_modify.name",
                "island.permission.inventory_modify.description"
        ));
    }
}
