package fr.euphyllia.skyllia.listeners.bukkitevents.player;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import net.kyori.adventure.text.Component;
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

    public MoveEvent() {

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleportOutside(final PlayerMoveEvent event) {
        if (!ConfigLoader.general.isTeleportOutsideIsland()) return;

        final Player player = event.getPlayer();
        if (player.hasPermission("skyllia.island.outside.bypass")) return;

        Location location = player.getLocation();
        World world = location.getWorld();
        if (world == null) return;

        if (!WorldUtils.isWorldSkyblock(world.getName())) return;

        int minHeight = world.getMinHeight();
        if (location.getBlockY() >= minHeight) return;

        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;

        Position position = RegionHelper.getRegionFromChunk(chunkX, chunkZ);

        Island island = SkylliaAPI.getIslandByPosition(position);
        if (island == null) return;

        WarpIsland homeWarp = island.getWarpByName("home");

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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDontLeaveIsland(final PlayerMoveEvent event) {
        if (!ConfigLoader.general.isRestrictPlayerMovement()) return;

        final Player player = event.getPlayer();
        if (player.hasPermission("skyllia.island.outside.bypass")) return;

        final Location to = event.getTo();
        if (to == null || to.getWorld() == null) return;

        if (!WorldUtils.isWorldSkyblock(to.getWorld().getName())) return;

        final Location from = event.getFrom();
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) return;

        int chunkX = to.getBlockX() >> 4;
        int chunkZ = to.getBlockZ() >> 4;

        Island island = ListenersUtils.checkChunkIsIsland(chunkX, chunkZ, from.getWorld(), event);
        if (island == null) return;

        Location center = RegionHelper.getCenterRegion(to.getWorld(), island.getPosition().x(), island.getPosition().z());
        if (!RegionHelper.isBlockWithinSquare(center, to.getBlockX(), to.getBlockZ(), island.getSize())) {
            player.teleportAsync(from, PlayerTeleportEvent.TeleportCause.PLUGIN).thenRun(() -> {
                Component component = ConfigLoader.language.translate(player, "island.player.outside-island", Map.of());
                if (component != null) player.sendActionBar(component);
            });
        }
    }
}
