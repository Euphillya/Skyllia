package fr.euphyllia.skyllia.listeners.permissions.player;

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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;

public class ItemDropPermissions implements PermissionModule {

    private PermissionId ITEM_DROP;


    @EventHandler(ignoreCancelled = true)
    public void onDrop(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final Location location = player.getLocation();

        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        final Chunk chunk = location.getChunk();
        final Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        final boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, ITEM_DROP);
        if (!hasPermission) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.ITEM_DROP = registry.register(new PermissionNode(
                new NamespacedKey(owner, "item.drop"),
                "Jeter des objets",
                "Placeholder"
        ));
    }
}
