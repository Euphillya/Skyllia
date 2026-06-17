package fr.euphyllia.skyllia.api.utils.models;

import fr.euphyllia.skyllia.api.skyblock.model.Position;

/**
 * @deprecated Use {@code Consumer<ChunkCoordinate>} instead.
 */
@Deprecated(forRemoval = false, since = "3.x")
@FunctionalInterface
public interface CallBackPosition {
    void run(Position position);
}