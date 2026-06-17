package fr.euphyllia.skyllia.api.skyblock.model;

import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a generic two-dimensional position.
 * <p>
 * This type was historically used for both chunk coordinates and
 * region coordinates, which could lead to ambiguity.
 * <p>
 * Use {@link fr.euphyllia.skyllia.api.coordinate.ChunkCoordinate}
 * when working with chunk coordinates, or
 * {@link fr.euphyllia.skyllia.api.coordinate.RegionCoordinate}
 * when working with region coordinates.
 *
 * @param x the X coordinate
 * @param z the Z coordinate
 * @deprecated since 3.x, replaced by
 * {@link fr.euphyllia.skyllia.api.coordinate.ChunkCoordinate}
 * and
 * {@link fr.euphyllia.skyllia.api.coordinate.RegionCoordinate}
 * to provide explicit coordinate types.
 */
@Deprecated(forRemoval = true, since = "3.x")
@ApiStatus.ScheduledForRemoval(inVersion = "4.x")
public record Position(int x, int z) {
}