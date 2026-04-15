package fr.euphyllia.skyllia.listeners;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

public class ListenersUtils {

    private ListenersUtils() {

    }

    public static @Nullable Island checkChunkIsIsland(Chunk chunk, Cancellable cancellable) {
        Position pos = RegionHelper.getRegionFromChunk(chunk.getX(), chunk.getZ());
        Island island = SkylliaAPI.getIslandByPosition(pos);

        if (island == null) {
            cancellable.setCancelled(true);
        }
        return island;
    }

    public static @Nullable Island checkChunkIsIsland(int chunkX, int chunkZ, World world, Cancellable cancellable) {
        Position pos = RegionHelper.getRegionFromChunk(chunkX, chunkZ);
        Island island = SkylliaAPI.getIslandByPosition(pos);

        if (island == null) {
            cancellable.setCancelled(true);
        }
        return island;
    }


    /**
     * Triggers a {@link PlayerPrepareChangeWorldSkyblockEvent} if the specified world is a Skyblock world.
     *
     * @param player      The {@link Player} transitioning to another world.
     * @param worldConfig
     * @param portalType  The type of portal being used.
     * @param cancellable Event can be canceled
     */
    public static void callPlayerPrepareChangeWorldSkyblockEvent(Player player, WorldConfig worldConfig, PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType, @Nullable Cancellable cancellable) {
        if (cancellable != null) cancellable.setCancelled(true);
        Bukkit.getPluginManager().callEvent(new PlayerPrepareChangeWorldSkyblockEvent(player, worldConfig, portalType));
    }

    /**
     * Checks whether a block is outside the island's boundaries (X/Z square AND
     * per-island build-height limits on the Y axis). If it is outside, the event
     * is cancelled.
     *
     * <p>Y limits are resolved in priority order:
     * <ol>
     *   <li>Island-specific custom value stored in {@code islands_build_height}</li>
     *   <li>World-config value ({@code min-y} / {@code height} from the TOML config)</li>
     *   <li>Bukkit's own {@link org.bukkit.World#getMinHeight()} /
     *       {@link org.bukkit.World#getMaxHeight()}</li>
     * </ol>
     * When no custom Y restriction has been set the Y check is skipped entirely,
     * preserving the original behaviour.
     *
     * @param island      The {@link Island} in question.
     * @param location    The {@link Location} to check.
     * @param cancellable The event that can be cancelled.
     * @return {@code true} if the block is outside the island boundaries, {@code false} otherwise.
     */
    public static boolean isBlockOutsideIsland(Island island, Location location, @Nullable Cancellable cancellable) {
        Position origin = island.getPosition();
        Location center = RegionHelper.getCenterRegion(location.getWorld(), origin.x(), origin.z());

        boolean outsideXZ = !RegionHelper.isBlockWithinSquare(center, location.getBlockX(), location.getBlockZ(), island.getSize());
        if (outsideXZ) {
            if (cancellable != null) {
                cancellable.setCancelled(true);
            }
            return true;
        }

        if (location.getWorld() != null) {
            World world = location.getWorld();
            String worldName = world.getName();

            Integer customMin = island.getBuildMinHeight(worldName);
            Integer customMax = island.getBuildMaxHeight(worldName);

            // Only enforce when at least one custom limit exists
            if (customMin != null || customMax != null) {
                int effectiveMin = resolveMin(world, customMin);
                int effectiveMax = resolveMax(world, customMax);

                if (location.getBlockY() < effectiveMin || location.getBlockY() > effectiveMax) {
                    if (cancellable != null) {
                        cancellable.setCancelled(true);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    private static int resolveMin(World world, @Nullable Integer customMin) {
        if (customMin != null) return customMin;
        WorldConfig wc = WorldUtils.getWorldConfig(world.getName());
        if (wc != null && wc.getWorldMinY() != null) return wc.getWorldMinY();
        return world.getMinHeight();
    }

    private static int resolveMax(World world, @Nullable Integer customMax) {
        if (customMax != null) return customMax;
        WorldConfig wc = WorldUtils.getWorldConfig(world.getName());
        if (wc != null && wc.getWorldHeight() != null) return wc.getWorldHeight();
        return world.getMaxHeight();
    }


    /**
     * Utility method that attempts to retrieve an {@link Island} based on a {@link Location}:
     * <ul>
     *     <li>Checks if the world is a Skyblock world</li>
     *     <li>Checks if the chunk corresponds to a valid island</li>
     * </ul>
     *
     * @param location    The {@link Location} to check.
     * @param cancellable The event that can be cancelled.
     * @return The {@link Island} if found, or {@code null} if not a Skyblock world or no island is found.
     */
    private static @Nullable Island getIslandFromLocation(Location location, Cancellable cancellable) {
        Chunk chunk = location.getChunk();
        String worldName = location.getWorld().getName();

        if (!WorldUtils.isWorldSkyblock(worldName)) return null;
        return checkChunkIsIsland(chunk, cancellable);
    }


}
