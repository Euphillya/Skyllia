package fr.euphyllia.skyllia.listeners.permissions.entity;

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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;

public class EntityInteractPermissions implements PermissionModule {

    private PermissionId ENTITY_INTERACT;

    @EventHandler(ignoreCancelled = true)
    public void onInteractEntity(final PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final Entity target = event.getRightClicked();
        final Location location = target.getLocation();

        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        final boolean hasBypass = player.hasPermission("skyllia.player.entity.interact.bypass");
        final boolean hasPermission = hasBypass || SkylliaAPI.getPermissionsManager().hasPermission(player, island, ENTITY_INTERACT, null, ConfigLoader.general.isDebugPermission());
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
        this.ENTITY_INTERACT = registry.register(new PermissionNode(
                new NamespacedKey(owner, "entity.interact"),
                "island.permission.entity_interact.name",
                "island.permission.entity_interact.description"
        ));
    }
}
