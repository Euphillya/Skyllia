package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@code IslandDataQuery} class defines an abstract set of methods
 * for querying and manipulating island data in a SkyBlock context.
 * <p>
 * Implementations should handle operations such as retrieving islands
 * by owner or player, creating new islands, and obtaining island details.
 */
public abstract class IslandDataQuery {

    /**
     * Retrieves an {@link Island} by the UUID of its owner.
     *
     * @param playerId the UUID of the island owner
     * @return a {@link CompletableFuture} that will complete with the retrieved {@link Island},
     * or {@code null} if no matching island is found
     */
    public abstract CompletableFuture<@Nullable Island> getIslandByOwnerId(UUID playerId);

    /**
     * Retrieves an {@link Island} by the UUID of a player who is a member of the island.
     *
     * @param playerId the UUID of the player
     * @return a {@link CompletableFuture} that will complete with the retrieved {@link Island},
     * or {@code null} if no matching island is found
     */
    public abstract CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerId);

    /**
     * Inserts a new island into the data source.
     *
     * @param futurIsland the {@link Island} to be inserted
     * @return a {@link CompletableFuture} that will complete with {@code true} if the insertion
     * was successful, or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> insertIslands(Island futurIsland);

    /**
     * Retrieves an {@link Island} by its UUID.
     *
     * @param islandId the UUID of the island
     * @return a {@link CompletableFuture} that will complete with the retrieved {@link Island},
     * or {@code null} if no island with the specified UUID is found
     */
    public abstract CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId);

    public abstract CompletableFuture<CopyOnWriteArrayList<Island>> getAllIslandsValid();

    /**
     * Retrieves the maximum number of members allowed in the specified {@link Island}.
     *
     * @param island the {@link Island} to check
     * @return a {@link CompletableFuture} that will complete with the maximum number of members
     */
    public abstract CompletableFuture<Integer> getMaxMemberInIsland(Island island);
}
