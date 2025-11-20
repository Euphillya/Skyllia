package fr.euphyllia.skyllia.api.utils.nms;

import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public abstract class ChunkImpl {

    /**
     * Iterates over each non-air block in the specified chunk and applies the given consumer.
     * @param world The world containing the chunk.
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @param consumer The consumer to apply to each non-air block.
     */
    public abstract void forEachNonAirBlockInChunk(
            @NotNull World world,
            int chunkX,
            int chunkZ,
            @NotNull ChunkBlockConsumer consumer
    );

    /**
     * A functional interface for consuming block data in a chunk.
     */
    @FunctionalInterface
    public interface ChunkBlockConsumer {
        void accept(int x, int y, int z, @NotNull Material data);
    }
}
