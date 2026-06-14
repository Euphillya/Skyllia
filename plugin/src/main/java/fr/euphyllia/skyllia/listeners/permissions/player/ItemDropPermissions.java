package fr.euphyllia.skyllia.listeners.permissions.player;

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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;

public class ItemDropPermissions implements PermissionModule {

    private PermissionId ITEM_DROP;


    @EventHandler(ignoreCancelled = true)
    public void onDrop(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final World world = player.getWorld();

        final int bx = Location.locToBlock(player.getX());
        final int by = Location.locToBlock(player.getY());
        final int bz = Location.locToBlock(player.getZ());

        final Island island = ListenersUtils.islandAtBlock(world, bx, bz);
        if (island == null) return;

        final boolean hasBypass = PlayerUtils.hasPermission(player, "skyllia.player.item.drop.bypass");
        final boolean hasPermission = hasBypass || SkylliaAPI.getPermissionsManager()
                .hasPermission(player, island, ITEM_DROP, null, ConfigLoader.general.getDebugSettings().permission());
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
        this.ITEM_DROP = registry.register(new PermissionNode(
                new NamespacedKey(owner, "item.drop"),
                "island.permission.item_drop.name",
                "island.permission.item_drop.description"
        ));
    }
}
