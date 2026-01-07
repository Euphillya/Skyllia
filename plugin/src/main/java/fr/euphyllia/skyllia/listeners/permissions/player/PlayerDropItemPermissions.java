package fr.euphyllia.skyllia.listeners.permissions.player;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.modules.PermissionModule;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.plugin.Plugin;

public class PlayerDropItemPermissions implements PermissionModule {

    private PermissionId PLAYER_DROP_ITEM;


    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final Location location = player.getLocation();

        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        Chunk chunk = location.getChunk();

        Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, PLAYER_DROP_ITEM);

        if (!hasPermission) {
            event.setCancelled(true);
        }
    }

    @Override
    public void registerPermissions(PermissionRegistry registry, Plugin owner) {
        this.PLAYER_DROP_ITEM = registry.register(new PermissionNode(
                new NamespacedKey(owner, "player.drop-item"),
                "Jeter des objets",
                "Autorise le joueur à jeter des objets au sol"
        ));
    }
}
