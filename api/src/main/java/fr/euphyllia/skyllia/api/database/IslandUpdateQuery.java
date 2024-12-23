package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;

import java.util.concurrent.CompletableFuture;

/**
 * The {@code IslandUpdateQuery} class defines an abstract set of methods
 * for updating various properties of an {@link Island} in a SkyBlock context.
 * <p>
 * Implementations should handle operations such as toggling the island status
 * (disable/private), adjusting maximum members, and resizing the island.
 */
public abstract class IslandUpdateQuery {

    /**
     * Updates the disabled status of the specified island.
     *
     * @param island  the {@link Island} to update
     * @param disable {@code true} to disable the island, {@code false} to enable it
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> updateDisable(Island island, boolean disable);

    /**
     * Updates the private status of the specified island.
     *
     * @param island        the {@link Island} to update
     * @param privateIsland {@code true} to set the island as private, {@code false} to make it public
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> updatePrivate(Island island, boolean privateIsland);

    /**
     * Checks if the specified island is disabled.
     *
     * @param island the {@link Island} to check
     * @return a {@link CompletableFuture} that completes with {@code true} if the island is disabled,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> isDisabledIsland(Island island);

    /**
     * Checks if the specified island is set to private.
     *
     * @param island the {@link Island} to check
     * @return a {@link CompletableFuture} that completes with {@code true} if the island is private,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> isPrivateIsland(Island island);

    /**
     * Sets the maximum number of members for the specified island.
     *
     * @param island   the {@link Island} to update
     * @param newValue the new maximum number of members
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> setMaxMemberInIsland(Island island, int newValue);

    /**
     * Sets the size of the specified island.
     *
     * @param island  the {@link Island} to update
     * @param newValue the new size of the island (e.g., radius or diameter, depending on implementation)
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> setSizeIsland(Island island, double newValue);
}
