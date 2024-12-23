package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The {@code IslandMemberQuery} class defines an abstract set of methods
 * for managing island members in a SkyBlock context.
 * <p>
 * Implementations should handle operations such as adding, updating,
 * and removing members, as well as retrieving specific members or owners.
 */
public abstract class IslandMemberQuery {

    /**
     * Updates the membership information of a given player within the specified {@link Island}.
     *
     * @param island  the {@link Island} the player belongs to
     * @param players the player's membership details
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> updateMember(Island island, Players players);

    /**
     * Retrieves the {@link Players} object associated with a specified island and player UUID.
     *
     * @param island   the {@link Island} to search
     * @param playerId the UUID of the player
     * @return a {@link CompletableFuture} that completes with the {@link Players} object
     */
    public abstract CompletableFuture<Players> getPlayersIsland(Island island, UUID playerId);

    /**
     * Retrieves the {@link Players} object associated with a specified island and player name.
     *
     * @param island     the {@link Island} to search
     * @param playerName the player's name
     * @return a {@link CompletableFuture} that completes with the {@link Players} object,
     *         or {@code null} if the player is not found
     */
    public abstract CompletableFuture<@Nullable Players> getPlayersIsland(Island island, String playerName);

    /**
     * Retrieves all members in the specified {@link Island}.
     *
     * @param island the {@link Island} whose members are to be retrieved
     * @return a {@link CompletableFuture} that completes with a list of {@link Players},
     *         or {@code null} if no members are found
     */
    public abstract CompletableFuture<@Nullable CopyOnWriteArrayList<Players>> getMembersInIsland(Island island);

    /**
     * Retrieves the owner of the specified {@link Island}.
     *
     * @param island the {@link Island} to query
     * @return a {@link CompletableFuture} that completes with the owner as a {@link Players} object,
     *         or {@code null} if no owner is found
     */
    public abstract CompletableFuture<@Nullable Players> getOwnerInIslandId(Island island);

    /**
     * Adds a "member clear" record to track a player's removal cause.
     *
     * @param playerId the UUID of the player
     * @param cause    the {@link RemovalCause} for the removal
     * @return a {@link CompletableFuture} that completes with {@code true} if the operation succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> addMemberClear(UUID playerId, RemovalCause cause);

    /**
     * Deletes a "member clear" record for the specified player and removal cause.
     *
     * @param playerId the UUID of the player
     * @param cause    the {@link RemovalCause} for the removal
     * @return a {@link CompletableFuture} that completes with {@code true} if the deletion succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> deleteMemberClear(UUID playerId, RemovalCause cause);

    /**
     * Checks if a "member clear" record exists for the specified player and removal cause.
     *
     * @param playerId the UUID of the player
     * @param cause    the {@link RemovalCause} for the removal
     * @return a {@link CompletableFuture} that completes with {@code true} if the record exists,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> checkClearMemberExist(UUID playerId, RemovalCause cause);

    /**
     * Deletes a member from the specified {@link Island}.
     *
     * @param island    the {@link Island} from which the member will be removed
     * @param oldMember the {@link Players} object representing the member to be removed
     * @return a {@link CompletableFuture} that completes with {@code true} if the removal succeeds,
     *         or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> deleteMember(Island island, Players oldMember);
}
