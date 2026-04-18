package fr.euphyllia.skyllia.listeners.permissions.block;

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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

public class BlockPlacePermissions implements PermissionModule {

    private PermissionId BLOCK_PLACE;

    @EventHandler(ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Location location = event.getBlockPlaced().getLocation();

        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        final boolean hasBypass = player.hasPermission("skyllia.player.place.bypass");
        final boolean hasPermission = hasBypass || SkylliaAPI.getPermissionsManager().hasPermission(player, island, BLOCK_PLACE, null, ConfigLoader.general.isDebugPermission());
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
        this.BLOCK_PLACE = registry.register(new PermissionNode(
                new NamespacedKey(owner, "block.place"),
                "island.permission.block_place.name",
                "island.permission.block_place.description"
        ));
    }
}
