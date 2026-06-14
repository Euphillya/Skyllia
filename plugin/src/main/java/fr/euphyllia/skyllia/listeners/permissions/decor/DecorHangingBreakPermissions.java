package fr.euphyllia.skyllia.listeners.permissions.decor;

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
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.plugin.Plugin;

public class DecorHangingBreakPermissions implements PermissionModule {

    private PermissionId DECOR_HANGING_BREAK;

    @EventHandler(ignoreCancelled = true)
    public void onBreak(final HangingBreakByEntityEvent event) {
        final Entity remover = event.getRemover();
        final Player player;
        if (remover instanceof Player p) {
            player = p;
        } else if (remover instanceof Projectile projectile
                && projectile.getShooter() instanceof Player shooter) {
            player = shooter;
        } else {
            return;
        }

        final Entity hanging = event.getEntity();
        final World world = hanging.getWorld();

        final int bx = Location.locToBlock(hanging.getX());
        final int by = Location.locToBlock(hanging.getY());
        final int bz = Location.locToBlock(hanging.getZ());

        final Island island = ListenersUtils.islandAtBlock(world, bx, bz);
        if (island == null) return;

        final boolean hasBypass = PlayerUtils.hasPermission(player, "skyllia.player.decor.hanging.break.bypass");
        final boolean hasPermission = hasBypass || SkylliaAPI.getPermissionsManager()
                .hasPermission(player, island, DECOR_HANGING_BREAK, null, ConfigLoader.general.getDebugSettings().permission());
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
        this.DECOR_HANGING_BREAK = registry.register(new PermissionNode(
                new NamespacedKey(owner, "decor.hanging.break"),
                "island.permission.decor_hanging_break.name",
                "island.permission.decor_hanging_break.description"
        ));
    }
}
