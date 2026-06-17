package fr.euphyllia.skyllia.api.utils.nms;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.coordinate.ChunkCoordinate;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import org.bukkit.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Provides methods for interacting with the Minecraft world using NMS (net.minecraft.server) classes.
 */
public abstract class WorldNMS {

    /**
     * Creates a new world using the specified WorldCreator.
     *
     * @param creator The WorldCreator to use for creating the world.
     * @return A FeedbackWorld object containing feedback about the world creation process.
     */
    public abstract WorldFeedback.FeedbackWorld createWorld(WorldCreator creator);

    /**
     * Creates a new world with optional custom height settings.
     * Implementations that support custom height should override this method.
     * The default implementation ignores the WorldConfig height settings and
     * falls back to {@link #createWorld(WorldCreator)}.
     *
     * @param creator     The WorldCreator to use for creating the world.
     * @param worldConfig The world configuration, potentially containing custom height settings.
     * @return A FeedbackWorld object containing feedback about the world creation process.
     */
    public WorldFeedback.FeedbackWorld createWorld(WorldCreator creator, WorldConfig worldConfig) {
        return createWorld(creator);
    }

    public abstract Map<Material, Integer> getCountAllBlocksInChunk(@NotNull World world, int chunkX, int chunkZ);

    /**
     * Resets a chunk at the specified chunk coordinate in the given world.
     *
     * @param craftWorld The world where the chunk is to be reset.
     * @param chunk      The chunk coordinate.
     */
    public abstract void resetChunk(World craftWorld, ChunkCoordinate chunk);

    /**
     * Resets a chunk at the specified position in the given world.
     *
     * @param craftWorld The world where the chunk is to be reset.
     * @param position   The position of the chunk to reset.
     * @deprecated Use {@link #resetChunk(World, ChunkCoordinate)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public void resetChunk(World craftWorld, Position position) {
        resetChunk(craftWorld, new ChunkCoordinate(position.x(), position.z()));
    }

    /**
     * Gets the current location TPS.
     *
     * @param location the location for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist
     */
    public abstract double @Nullable [] getTPS(Location location);

    /**
     * Gets the current chunk TPS.
     *
     * @param chunk the chunk for which to get the TPS
     * @return current location TPS (5s, 15s, 1m, 5m, 15m in Folia-Server), or null if the region doesn't exist
     */
    public abstract double @Nullable [] getTPS(Chunk chunk);

    /**
     * Gets the average tick times for a specific location.
     *
     * @param location the location for which to get the average tick times
     * @return an array of average tick times, or null if the region doesn't exist
     */
    public abstract double @Nullable [] getAverageTickTimes(Location location);

    /**
     * Gets the average tick times for a specific chunk.
     *
     * @param chunk the chunk for which to get the average tick times
     * @return an array of average tick times, or null if the region doesn't exist
     */
    public abstract double @Nullable [] getAverageTickTimes(Chunk chunk);
}
