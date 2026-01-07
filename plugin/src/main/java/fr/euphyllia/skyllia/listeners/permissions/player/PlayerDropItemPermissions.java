package fr.euphyllia.skyllia.listeners.permissions.player;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemPermissions implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        final Location location = player.getLocation();

        if (!SkylliaAPI.isWorldSkyblock(location.getWorld())) return;

        Chunk chunk = location.getChunk();

        Island island = SkylliaAPI.getIslandByChunk(chunk);
        if (island == null) return;

        boolean hasPermission = SkylliaAPI.getPermissionsManager().hasPermission(player, island, null);

        if (!hasPermission) {
            event.setCancelled(true);
        }
    }
}
