package fr.euphyllia.skyllia.api.coordinate;

/**
 * Represents the coordinates of a region file.
 * <p>
 * The X and Z values correspond to Minecraft region coordinates
 * used by {@code .mca} files.
 *
 * @param x the region X coordinate
 * @param z the region Z coordinate
 * @since 3.x
 */
public record RegionCoordinate(int x, int z)
        implements Coordinate2D {
}