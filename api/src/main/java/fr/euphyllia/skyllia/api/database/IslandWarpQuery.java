package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

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
     * Updates (or creates) a named warp for the specified island at the given {@link Location}.
     *
     * @param island   the {@link Island} to update
     * @param warpName the name of the warp point
     * @param location the {@link Location} to set for this warp
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> updateWarp(Island island, String warpName, Location location);

    /**
     * Retrieves a warp by name for the specified island.
     *
     * @param island   the {@link Island} whose warp is to be retrieved
     * @param warpName the name of the warp point
     * @return a {@link CompletableFuture} that completes with the {@link WarpIsland} object,
     *         or {@code null} if no warp with the specified name is found
     */
    public abstract CompletableFuture<@Nullable WarpIsland> getWarpByName(Island island, String warpName);

    /**
     * Retrieves all warps defined for the specified island.
     *
     * @param island the {@link Island} to query
     * @return a {@link CompletableFuture} that completes with a list of {@link WarpIsland} objects,
     *         or {@code null} if no warps are found
     */
    public abstract CompletableFuture<@Nullable CopyOnWriteArrayList<WarpIsland>> getListWarp(Island island);

    /**
     * Deletes a named warp for the specified island.
     *
     * @param island the {@link Island} to update
     * @param name   the name of the warp point to delete
     * @return a {@link CompletableFuture} that completes with {@code true} if the deletion succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> deleteWarp(Island island, String name);
}
