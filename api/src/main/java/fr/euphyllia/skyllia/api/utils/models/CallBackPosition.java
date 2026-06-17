package fr.euphyllia.skyllia.api.utils.models;

import fr.euphyllia.skyllia.api.skyblock.model.Position;
import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated Use {@code Consumer<ChunkCoordinate>} instead.
 */
@Deprecated(forRemoval = true, since = "3.x")
@ApiStatus.ScheduledForRemoval(inVersion = "4.x")
@FunctionalInterface
public interface CallBackPosition {
    void run(Position position);
}