package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.island.PositionIslandCache;
import fr.euphyllia.skyllia.cache.island.WarpsInIslandCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import java.util.Map;

public class MoveEvent implements Listener {

    public MoveEvent(InterneAPI interneAPI) {
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleportOutside(final PlayerMoveEvent event) {
        if (!ConfigLoader.general.isTeleportOutsideIsland()) return;
        final Player player = event.getPlayer();
        if (PermissionImp.hasPermission(player, "skyllia.island.outside.bypass")) return;

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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDontLeaveIsland(final PlayerMoveEvent event) {
        final Location to = event.getTo();
        final Player player = event.getPlayer();
        if (!ConfigLoader.general.isRestrictPlayerMovement()) return;

        if (!WorldUtils.isWorldSkyblock(to.getWorld().getName())) return;

        if (PermissionImp.hasPermission(player, "skyllia.island.outside.bypass")) return;
        if (event.getFrom().getBlockX() == to.getBlockX() && event.getFrom().getBlockZ() == to.getBlockZ()) return;
        int chunkX = to.getBlockX() >> 4;
        int chunkZ = to.getBlockZ() >> 4;
        Island island = ListenersUtils.checkChunkIsIsland(chunkX, chunkZ, event);
        if (island == null) return;

        Location center = RegionHelper.getCenterRegion(to.getWorld(), island.getPosition().x(), island.getPosition().z());
        if (!RegionHelper.isBlockWithinSquare(center, to.getBlockX(), to.getBlockZ(), island.getSize())) {
            player.teleportAsync(event.getFrom());
            Component component = ConfigLoader.language.translate(player, "island.player.outside-island", Map.of());
            if (component != null) {
                player.sendActionBar(component);
            }
        }
    }
}
