package fr.euphyllia.skyllia.api.coordinate;

/**
 * Represents a two-dimensional coordinate using X and Z values.
 * <p>
 * This interface is used as a common contract for coordinate types
 * such as chunk coordinates and region coordinates.
 *
 * @since 3.x
 */
public sealed interface Coordinate2D
        permits ChunkCoordinate, RegionCoordinate {

    /**
     * Returns the X coordinate.
     *
     * @return the X coordinate
     */
    int x();

    /**
     * Returns the Z coordinate.
     *
     * @return the Z coordinate
     */
    int z();
}