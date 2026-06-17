package fr.euphyllia.skyllia.api.coordinate;

/**
 * Represents the coordinates of a chunk.
 * <p>
 * The X and Z values correspond to Minecraft chunk coordinates.
 *
 * @param x the chunk X coordinate
 * @param z the chunk Z coordinate
 * @since 3.x
 */
public record ChunkCoordinate(int x, int z)
        implements Coordinate2D {
}