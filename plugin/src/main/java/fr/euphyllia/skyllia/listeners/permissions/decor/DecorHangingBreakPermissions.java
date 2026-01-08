package fr.euphyllia.skyllia.listeners.permissions.decor;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Chunk;
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

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, DECOR_HANGING_BREAK);
        if (!hasPermission) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.DECOR_HANGING_BREAK = registry.register(new PermissionNode(
                new NamespacedKey(owner, "decor.hanging.break"),
                "Casser des décorations (cadres/tableaux)",
                "Placeholder"
        ));
    }
}
