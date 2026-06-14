package fr.euphyllia.skyllia.listeners.permissions.block;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.PlayerUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class BlockPhysicalPermissions implements PermissionModule {

    private PermissionId BLOCK_PHYSICAL;

    @EventHandler(ignoreCancelled = true)
    public void onPhysical(final PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) return;

        final Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        final World world = clicked.getWorld();

        final int bx = clicked.getX();
        final int by = clicked.getY();
        final int bz = clicked.getZ();

        final Island island = ListenersUtils.islandAtBlock(world, bx, bz);
        if (island == null) return;

        final Player player = event.getPlayer();
        final boolean hasBypass = PlayerUtils.hasPermission(player, "skyllia.player.physical.bypass");
        final boolean hasPermission = hasBypass || SkylliaAPI.getPermissionsManager()
                .hasPermission(player, island, BLOCK_PHYSICAL, null, ConfigLoader.general.getDebugSettings().permission());
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
        this.BLOCK_PHYSICAL = registry.register(new PermissionNode(
                new NamespacedKey(owner, "block.physical"),
                "island.permission.block_physical.name",
                "island.permission.block_physical.description"
        ));
    }
}
