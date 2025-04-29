package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.island.PositionIslandCache;
import fr.euphyllia.skyllia.cache.island.WarpsInIslandCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

public class MoveEvent implements Listener {

    public MoveEvent(InterneAPI interneAPI) {
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleportOutside(final PlayerMoveEvent event) {
        if (!ConfigLoader.general.isTeleportOutsideIsland()) return;
        final Player player = event.getPlayer();
        Location location = player.getLocation();
        World world = location.getWorld();
        if (!WorldUtils.isWorldSkyblock(world.getName())) return;
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        Position position = RegionHelper.getRegionFromChunk(chunkX, chunkZ);
        Island island = PositionIslandCache.getIsland(position);
        if (island == null) return;
        int minHeight = world.getMinHeight();
        if (location.getBlockY() < minHeight) {
            var islandHomePosition = WarpsInIslandCache.getWarpsCached(island.getId());
            WarpIsland homeWarp = islandHomePosition.stream()
                    .filter(warp -> warp.warpName().equalsIgnoreCase("home"))
                    .findFirst()
                    .orElse(null);
            if (homeWarp != null && homeWarp.location() != null && homeWarp.location().getWorld() != null) {
                Location homeLocation = homeWarp.location().clone();
                homeLocation.setY(homeLocation.getY() + 0.5);
                player.teleportAsync(homeLocation, PlayerTeleportEvent.TeleportCause.PLUGIN).thenRun(() -> {
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                });
            } else {
                Location centerLocation = RegionHelper.getCenterRegion(world, position.x(), position.z());
                player.teleportAsync(centerLocation, PlayerTeleportEvent.TeleportCause.PLUGIN).thenRun(() -> {
                    player.setVelocity(new Vector(0, 0, 0));
                    player.setFallDistance(0);
                });
            }
        }
    }
}
