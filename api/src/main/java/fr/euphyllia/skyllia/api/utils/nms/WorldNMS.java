package fr.euphyllia.skyllia.api.utils.nms;

import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.Nullable;

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
     * Resets a chunk at the specified position in the given world.
     *
     * @param craftWorld The world where the chunk is to be reset.
     * @param position   The position of the chunk to reset.
     */
    public abstract void resetChunk(World craftWorld, Position position);

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
}
