package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;
import java.util.concurrent.CompletableFuture;

/**
 * The {@code IslandPermissionQuery} class defines an abstract set of methods
 * for managing island permissions in a SkyBlock context.
 * <p>
 * Implementations should handle permission updates and retrieval for different
 * roles and game rule configurations.
 */
public abstract class IslandPermissionQuery {

    /**
     * Retrieves the game rule value (usually represented as a long) for the specified island.
     *
     * @param island the {@link Island} whose game rule is to be retrieved
     * @return a {@link CompletableFuture} that completes with the game rule value
     */
    public abstract Long getIslandGameRule(Island island);

    /**
     * Updates the game rule value for the specified island.
     *
     * @param island the {@link Island} whose game rule is to be updated
     * @param value  the new game rule value as a {@code long}
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     * or {@code false} otherwise
     */
    public abstract Boolean updateIslandGameRule(Island island, long value);
}
