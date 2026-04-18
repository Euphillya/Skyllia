package fr.euphyllia.skyllia.listeners.permissions.decor;

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
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.plugin.Plugin;

public class DecorHangingBreakPermissions implements PermissionModule {

    private PermissionId DECOR_HANGING_BREAK;

    @EventHandler(ignoreCancelled = true)
    public void onBreak(final HangingBreakByEntityEvent event) {
        final Entity remover = event.getRemover();
        if (!(remover instanceof Player player)) return;

        final Location location = event.getEntity().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final int chunkX = location.getBlockX() >> 4;
        final int chunkZ = location.getBlockZ() >> 4;
        final Island island = SkylliaAPI.getIslandByChunk(chunkX, chunkZ);
        if (island == null) return;

        final boolean hasBypass = player.hasPermission("skyllia.player.decor.hanging.break.bypass");
        final boolean hasPermission = hasBypass || SkylliaAPI.getPermissionsManager().hasPermission(player, island, DECOR_HANGING_BREAK, null, ConfigLoader.general.isDebugPermission());
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
        this.DECOR_HANGING_BREAK = registry.register(new PermissionNode(
                new NamespacedKey(owner, "decor.hanging.break"),
                "island.permission.decor_hanging_break.name",
                "island.permission.decor_hanging_break.description"
        ));
    }
}
