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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.plugin.Plugin;

public class DecorHangingPlacePermissions implements PermissionModule {

    private PermissionId DECOR_HANGING_PLACE;

    @EventHandler(ignoreCancelled = true)
    public void onPlace(final HangingPlaceEvent event) {
        final Player player = event.getPlayer();
        if (player == null) return;

        final Location location = event.getEntity().getLocation();
        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, DECOR_HANGING_PLACE);
        if (!hasPermission) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.DECOR_HANGING_PLACE = registry.register(new PermissionNode(
                new NamespacedKey(owner, "decor.hanging.place"),
                "Placer des décorations (cadres/tableaux)",
                "Placeholder"
        ));
    }
}
