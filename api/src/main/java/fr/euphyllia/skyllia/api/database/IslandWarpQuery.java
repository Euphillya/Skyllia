package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.world.SkylliaLocation;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@code IslandWarpQuery} class defines an abstract set of methods
 * for managing warp points (teleport locations) in a SkyBlock context.
 * <p>
 * Implementations should handle creating, retrieving, and deleting warp points.
 */
public abstract class IslandWarpQuery {

    /**
     * Updates (or creates) a named warp for the specified island at the given {@link SkylliaLocation}.
     *
     * @param islandId the UUID of the island whose warp is to be updated
     * @param warpName the name of the warp point
     * @param location the {@link SkylliaLocation} to set for this warp
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     * or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> updateWarp(UUID islandId, String warpName, SkylliaLocation location);

    /**
     * Retrieves a warp by name for the specified island.
     *
     * @param islandId the UUID of the island whose warp is to be retrieved
     * @param warpName the name of the warp point
     * @return a {@link CompletableFuture} that completes with the {@link WarpIsland} object,
     * or {@code null} if no warp with the specified name is found
     */
    public abstract CompletableFuture<@Nullable WarpIsland> getWarpByName(UUID islandId, String warpName);

    /**
     * Retrieves all warps defined for the specified island.
     *
     * @param islandId the UUID of the island whose warps are to be retrieved
     * @return a {@link CompletableFuture} that completes with a list of {@link WarpIsland} objects,
     * or {@code null} if no warps are found
     */
    public abstract CompletableFuture<@Nullable CopyOnWriteArrayList<WarpIsland>> getListWarp(UUID islandId);

    /**
     * Deletes a named warp for the specified island.
     *
     * @param islandId the UUID of the island whose warp is to be deleted
     * @param name     the name of the warp point to delete
     * @return a {@link CompletableFuture} that completes with {@code true} if the deletion succeeds,
     * or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> deleteWarp(UUID islandId, String name);
}
