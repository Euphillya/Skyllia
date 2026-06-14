package fr.euphyllia.skyllia.listeners.permissions.entity;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
        final World world = target.getWorld();

        final int bx = Location.locToBlock(target.getX());
        final int by = Location.locToBlock(target.getY());
        final int bz = Location.locToBlock(target.getZ());

        final Island island = ListenersUtils.islandAtBlock(world, bx, bz);
        if (island == null) return;

        final boolean hasBypass = PlayerUtils.hasPermission(player, "skyllia.player.entity.interact.bypass");
        final boolean hasPermission = hasBypass || SkylliaAPI.getPermissionsManager()
                .hasPermission(player, island, ENTITY_INTERACT, null, ConfigLoader.general.getDebugSettings().permission());
        if (!hasPermission) {
            event.setCancelled(true);
            return;
        }
        if (!hasBypass) {
            ListenersUtils.isBlockOutsideIsland(island, world, bx, by, bz, event);
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
