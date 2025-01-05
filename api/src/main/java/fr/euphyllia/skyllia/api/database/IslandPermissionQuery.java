package fr.euphyllia.skyllia.api.database;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;

import java.util.UUID;
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
     * Updates the permissions for a specific {@link PermissionsType} and {@link RoleType}
     * on the given island.
     *
     * @param island          the {@link Island} whose permissions are to be updated
     * @param permissionsType the type of permission to update
     * @param roleType        the role type (e.g., OWNER, MEMBER) for which permissions are updated
     * @param permissions     the new permission flags as a {@code long} value
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     * or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> updateIslandsPermission(
            Island island,
            PermissionsType permissionsType,
            RoleType roleType,
            long permissions
    );

    /**
     * Retrieves the {@link PermissionRoleIsland} object representing permissions
     * for the specified {@link PermissionsType} and {@link RoleType} on the given island.
     *
     * @param islandId        the UUID of the island
     * @param permissionsType the type of permission to retrieve
     * @param roleType        the role type (e.g., OWNER, MEMBER)
     * @return a {@link CompletableFuture} that completes with the corresponding
     * {@link PermissionRoleIsland} object
     */
    public abstract CompletableFuture<PermissionRoleIsland> getIslandPermission(
            UUID islandId,
            PermissionsType permissionsType,
            RoleType roleType
    );

    /**
     * Retrieves the game rule value (usually represented as a long) for the specified island.
     *
     * @param island the {@link Island} whose game rule is to be retrieved
     * @return a {@link CompletableFuture} that completes with the game rule value
     */
    public abstract CompletableFuture<Long> getIslandGameRule(Island island);

    /**
     * Updates the game rule value for the specified island.
     *
     * @param island the {@link Island} whose game rule is to be updated
     * @param value  the new game rule value as a {@code long}
     * @return a {@link CompletableFuture} that completes with {@code true} if the update succeeds,
     * or {@code false} otherwise
     */
    public abstract CompletableFuture<Boolean> updateIslandGameRule(Island island, long value);
}
