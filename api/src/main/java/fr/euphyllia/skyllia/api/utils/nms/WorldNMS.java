package fr.euphyllia.skyllia.api.utils.nms;

import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.world.WorldFeedback;
import org.bukkit.World;
import org.bukkit.WorldCreator;

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
     * @param position The position of the chunk to reset.
     */
    public abstract void resetChunk(World craftWorld, Position position);
}
