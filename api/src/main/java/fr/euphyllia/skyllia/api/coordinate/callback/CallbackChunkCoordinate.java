package fr.euphyllia.skyllia.api.coordinate.callback;

import fr.euphyllia.skyllia.api.coordinate.ChunkCoordinate;

/**
 * Callback invoked for a chunk coordinate.
 *
 * @since 3.x
 */
@FunctionalInterface
public interface CallbackChunkCoordinate {

    /**
     * Called with the chunk coordinate.
     *
     * @param coordinate the chunk coordinate
     */
    void run(ChunkCoordinate coordinate);
}