package fr.euphyllia.skyllia.listeners;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.island.PositionIslandCache;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

public class ListenersUtils {

    private ListenersUtils() {

    }

    /**
     * Retrieves the {@link Island} from a given {@link Chunk}, cancelling the event if no island is found.
     *
     * @param chunk       The {@link Chunk} to check.
     * @param cancellable The event that can be cancelled.
     * @return The {@link Island} if found, or {@code null} otherwise.
     */
    public static @Nullable Island checkChunkIsIsland(Chunk chunk, Cancellable cancellable) {
        Position position = RegionHelper.getRegionFromChunk(chunk.getX(), chunk.getZ());
        Island island = PositionIslandCache.getIsland(position);
        if (island == null) {
            cancellable.setCancelled(true); // Sécurité !
        }
        return island;
    }

    /**
     * Retrieves the {@link Island} from a given chunk coordinates, cancelling the event if no island is found.
     *
     * @param chunkX      The X coordinate of the chunk.
     * @param chunkZ      The Z coordinate of the chunk.
     * @param cancellable The event that can be cancelled.
     * @return The {@link Island} if found, or {@code null} otherwise.
     */
    public static @Nullable Island checkChunkIsIsland(int chunkX, int chunkZ, Cancellable cancellable) {
        Position position = RegionHelper.getRegionFromChunk(chunkX, chunkZ);
        Island island = PositionIslandCache.getIsland(position);
        if (island == null) {
            cancellable.setCancelled(true); // Sécurité !
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
     * Checks whether a block is outside the island's boundaries. If it is outside, the event is cancelled.
     *
     * @param island      The {@link Island} in question.
     * @param location    The {@link Location} to check.
     * @param cancellable The event that can be cancelled.
     * @return {@code true} if the block is outside the island boundaries, {@code false} otherwise.
     */
    public static boolean isBlockOutsideIsland(Island island, Location location, Cancellable cancellable) {
        Position origin = island.getPosition();
        boolean outsideIsland = !RegionHelper.isBlockWithinSquare(
                RegionHelper.getCenterRegion(location.getWorld(), origin.x(), origin.z()),
                location.getBlockX(), location.getBlockZ(), island.getSize()
        );
        if (outsideIsland) {
            cancellable.setCancelled(true);
        }
        return outsideIsland;
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
