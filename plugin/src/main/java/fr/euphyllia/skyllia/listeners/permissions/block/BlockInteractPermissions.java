package fr.euphyllia.skyllia.listeners.permissions.block;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class BlockInteractPermissions implements PermissionModule {

    private PermissionId BLOCK_INTERACT;

    @EventHandler(ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        final Player player = event.getPlayer();
        final Location location = clicked.getLocation();

        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, BLOCK_INTERACT);
        if (!hasPermission) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.BLOCK_INTERACT = registry.register(new PermissionNode(
                new NamespacedKey(owner, "block.interact"),
                "Interagir avec les blocs",
                "Placeholder"
        ));
    }
}
