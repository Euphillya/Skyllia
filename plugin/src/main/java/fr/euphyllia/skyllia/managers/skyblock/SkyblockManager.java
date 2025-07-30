package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.event.PrepareSkyblockCreateEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the creation, retrieval, and modification of Skyblock islands, including member management,
 * island permissions, warps, and other properties.
 */
public class SkyblockManager {

    private static final Logger LOGGER = LogManager.getLogger(SkyblockManager.class);
    private final Skyllia plugin;

    public SkyblockManager(Skyllia Skyllia) {
        this.plugin = Skyllia;
    }

    /**
     * Creates a new island with the specified {@link IslandSettings}.
     *
     * @param islandId   The UUID of the new island.
     * @param islandType The settings to apply to the new island.
     * @return A {@link CompletableFuture} that completes with {@code true} if the island was created,
     * or {@code false} if creation was cancelled or an error occurred.
     */
    public CompletableFuture<Boolean> createIsland(UUID islandId, IslandSettings islandType) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        PrepareSkyblockCreateEvent event = new PrepareSkyblockCreateEvent(islandId, islandType);
        Bukkit.getPluginManager().callEvent(event);

        // Check if the event was cancelled
        if (event.isCancelled()) {
            future.complete(false);
            return future;
        }

        try {
            // Construct a new IslandHook instance before inserting it into the database
            Island futureIsland = new IslandHook(
                    this.plugin,
                    event.getIslandId(),
                    event.getIslandSettings().maxMembers(),
                    null,
                    event.getIslandSettings().rayon(),
                    null
            );
            // Insert into database (async)
            this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery()
                    .insertIslands(futureIsland)
                    // This callback is executed once the insertion is completed successfully
                    .thenAcceptAsync(future::complete)
                    // This handles any exception that occurs in the asynchronous chain
                    .exceptionally(throwable -> {
                        LOGGER.fatal("Exception while creating a new island asynchronously", throwable);
                        future.complete(false);
                        return null;
                    });
        } catch (Exception e) {
            LOGGER.fatal("Exception before starting async creation", e);
            future.complete(false);
        }
        return future;
    }

    /**
     * Retrieves an island by its unique islandId.
     *
     * @param islandId The UUID of the island.
     * @return A {@link CompletableFuture} containing the {@link Island}, or {@code null} if not found.
     */
    public CompletableFuture<Island> getIslandByIslandId(UUID islandId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByIslandId(islandId);
    }

    public CompletableFuture<CopyOnWriteArrayList<Island>> getAllIslandsValid() {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getAllIslandsValid();
    }

    /**
     * Disables or enables an island.
     *
     * @param island   The {@link Island} to update.
     * @param disabled {@code true} to disable, {@code false} to enable.
     * @return A {@link CompletableFuture} with {@code true} if successfully updated, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> disableIsland(Island island, boolean disabled) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updateDisable(island, disabled);
    }

    /**
     * Checks if an island is currently disabled.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with {@code true} if disabled, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> isDisabledIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isDisabledIsland(island);
    }

    /**
     * Sets whether an island is private.
     *
     * @param island        The {@link Island}.
     * @param privateIsland {@code true} for private, {@code false} for public.
     * @return A {@link CompletableFuture} with {@code true} if the state was successfully updated.
     */
    public CompletableFuture<Boolean> setPrivateIsland(Island island, boolean privateIsland) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updatePrivate(island, privateIsland);
    }

    /**
     * Checks if an island is private.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with {@code true} if private, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> isPrivateIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isPrivateIsland(island);
    }

    /**
     * Retrieves the island owned by a specific player.
     *
     * @param playerId The player's UUID.
     * @return A {@link CompletableFuture} that completes with the {@link Island}, or {@code null} if none.
     */
    public CompletableFuture<@Nullable Island> getIslandByOwner(UUID playerId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByOwnerId(playerId);
    }

    /**
     * Retrieves any island the player is a member of (not necessarily the owner).
     *
     * @param playerId The player's UUID.
     * @return A {@link CompletableFuture} that completes with the {@link Island}, or {@code null} if none.
     */
    public CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByPlayerId(playerId);
    }

    /**
     * Adds a warp to an island.
     *
     * @param islandId       The UUID of the island.
     * @param name           The name of the warp.
     * @param playerLocation The warp location.
     * @return A {@link CompletableFuture} with {@code true} if successfully added, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> addWarpsIsland(UUID islandId, String name, Location playerLocation) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().updateWarp(islandId, name, playerLocation);
    }

    /**
     * Deletes a warp from an island.
     *
     * @param islandId The UUID of the island.
     * @param name     The warp name.
     * @return A {@link CompletableFuture} with {@code true} if deleted, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> delWarpsIsland(UUID islandId, String name) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().deleteWarp(islandId, name);
    }

    /**
     * Retrieves a warp by its name from an island.
     *
     * @param islandId The UUID of the island.
     * @param name     The warp name.
     * @return A {@link CompletableFuture} with the {@link WarpIsland}, or {@code null} if not found.
     */
    public CompletableFuture<@Nullable WarpIsland> getWarpIslandByName(UUID islandId, String name) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().getWarpByName(islandId, name);
    }

    /**
     * Retrieves all warps for an island.
     *
     * @param islandId The UUID of the island.
     * @return A {@link CompletableFuture} containing a list of {@link WarpIsland} objects.
     */
    public CompletableFuture<@Nullable CopyOnWriteArrayList<WarpIsland>> getWarpsIsland(UUID islandId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().getListWarp(islandId);
    }

    /**
     * Updates or adds a member on the specified island.
     *
     * @param island  The {@link Island}.
     * @param players The {@link Players} object representing the member.
     * @return A {@link CompletableFuture} with {@code true} if successfully updated, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updateMember(Island island, Players players) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().updateMember(island, players);
    }

    /**
     * Retrieves all members on the specified island.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with a list of members in a {@link CopyOnWriteArrayList}.
     */
    public CompletableFuture<CopyOnWriteArrayList<Players>> getMembersInIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getMembersInIsland(island);
    }

    /**
     * Gets a specific member by their UUID from the island.
     *
     * @param island   The {@link Island}.
     * @param playerId The player's UUID.
     * @return A {@link CompletableFuture} with the member {@link Players} object.
     */
    public CompletableFuture<Players> getMemberInIsland(Island island, UUID playerId) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getPlayersIsland(island, playerId);
    }

    /**
     * Gets a specific member by name from the island.
     *
     * @param island     The {@link Island}.
     * @param playerName The player's name.
     * @return A {@link CompletableFuture} with the member {@link Players} object or {@code null} if not found.
     */
    public CompletableFuture<@Nullable Players> getMemberInIsland(Island island, String playerName) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getPlayersIsland(island, playerName);
    }

    /**
     * Adds a record to clear a member on their next login.
     *
     * @param playerId The player's UUID.
     * @param cause    The {@link RemovalCause}.
     * @return A {@link CompletableFuture} with {@code true} if successful, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> addClearMemberNextLogin(UUID playerId, RemovalCause cause) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().addMemberClear(playerId, cause);
    }

    /**
     * Deletes a scheduled member clear record.
     *
     * @param playerId The player's UUID.
     * @param cause    The {@link RemovalCause}.
     * @return A {@link CompletableFuture} with {@code true} if deleted, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> deleteClearMember(UUID playerId, RemovalCause cause) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().deleteMemberClear(playerId, cause);
    }

    /**
     * Checks if a clear member record exists.
     *
     * @param playerId The player's UUID.
     * @param cause    The {@link RemovalCause}.
     * @return A {@link CompletableFuture} with {@code true} if exists, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> checkClearMemberExist(UUID playerId, RemovalCause cause) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().checkClearMemberExist(playerId, cause);
    }

    /**
     * Updates the island permission for a specific role.
     *
     * @param island          The {@link Island}.
     * @param permissionsType The {@link PermissionsType}.
     * @param roleType        The {@link RoleType}.
     * @param permissions     The new permission value.
     * @return A {@link CompletableFuture} with {@code true} if updated, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updatePermissionIsland(Island island,
                                                             PermissionsType permissionsType,
                                                             RoleType roleType,
                                                             long permissions) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandPermissionQuery()
                .updateIslandsPermission(island, permissionsType, roleType, permissions);
    }

    /**
     * Retrieves the permission value for the given role and permission type on an island.
     *
     * @param islandId        The island's UUID.
     * @param permissionsType The {@link PermissionsType}.
     * @param roleType        The {@link RoleType}.
     * @return A {@link CompletableFuture} with a {@link PermissionRoleIsland}.
     */
    public CompletableFuture<PermissionRoleIsland> getPermissionIsland(UUID islandId,
                                                                       PermissionsType permissionsType,
                                                                       RoleType roleType) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandPermissionQuery()
                .getIslandPermission(islandId, permissionsType, roleType);
    }

    /**
     * Updates the game rule value for the island.
     *
     * @param island        The {@link Island}.
     * @param gameRuleValue The new game rule value.
     * @return A {@link CompletableFuture} with {@code true} if updated, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> updateGamerule(Island island, long gameRuleValue) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandPermissionQuery().updateIslandGameRule(island, gameRuleValue);
    }

    /**
     * Retrieves the current game rule permission value for the island.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with the game rule permission as a {@code long}.
     */
    public CompletableFuture<Long> getGameRulePermission(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandPermissionQuery().getIslandGameRule(island);
    }

    /**
     * Deletes a member from the island.
     *
     * @param island    The {@link Island}.
     * @param oldMember The {@link Players} object representing the member to delete.
     * @return A {@link CompletableFuture} with {@code true} if deleted, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> deleteMember(Island island, Players oldMember) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().deleteMember(island, oldMember);
    }

    /**
     * Retrieves the owner of the island.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with the {@link Players} object of the owner, or null if none.
     */
    public CompletableFuture<@Nullable Players> getOwnerByIslandID(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getOwnerInIslandId(island);
    }

    /**
     * Retrieves the maximum number of members allowed in the island.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with the max member count, or -1 if not found.
     */
    public CompletableFuture<Integer> getMaxMemberInIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getMaxMemberInIsland(island);
    }

    /**
     * Sets the maximum number of members allowed in the island.
     *
     * @param island   The {@link Island}.
     * @param newValue The new maximum.
     * @return A {@link CompletableFuture} with {@code true} if successfully updated, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> setMaxMemberInIsland(Island island, int newValue) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().setMaxMemberInIsland(island, newValue);
    }

    /**
     * Sets the size of the island.
     *
     * @param island   The {@link Island}.
     * @param newValue The new size (radius).
     * @return A {@link CompletableFuture} with {@code true} if successfully updated, {@code false} otherwise.
     */
    public CompletableFuture<Boolean> setSizeIsland(Island island, double newValue) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().setSizeIsland(island, newValue);
    }

    /**
     * Met à jour l'état "locked" de l'île (vaut true pendant un delete).
     */
    public CompletableFuture<Boolean> setLockedIsland(Island island, boolean locked) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().setLockedIsland(island, locked);
    }

    /**
     * Vérifie si l'île est actuellement "locked".
     */
    public CompletableFuture<Boolean> isLockedIsland(Island island) {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isLockedIsland(island);
    }

}
