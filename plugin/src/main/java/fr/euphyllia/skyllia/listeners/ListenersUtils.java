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
     * Checks whether a block is outside the island's boundaries. If it is outside, the event is cancelled.
     *
     * @param island      The {@link Island} in question.
     * @param location    The {@link Location} to check.
     * @param cancellable The event that can be cancelled.
     * @return {@code true} if the block is outside the island boundaries, {@code false} otherwise.
     */
    public static boolean isBlockOutsideIsland(Island island, Location location, Cancellable cancellable) {
        Position origin = island.getPosition();
        Location center = RegionHelper.getCenterRegion(location.getWorld(), origin.x(), origin.z());
        boolean outside = !RegionHelper.isBlockWithinSquare(center, location.getBlockX(), location.getBlockZ(), island.getSize());
        if (outside) cancellable.setCancelled(true);
        return outside;
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
