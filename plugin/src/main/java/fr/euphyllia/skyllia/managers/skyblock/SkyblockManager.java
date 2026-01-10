package fr.euphyllia.skyllia.managers.skyblock;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.event.PrepareSkyblockCreateEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.enums.RemovalCause;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.cache.SkyblockCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages the creation, retrieval, and modification of Skyblock islands, including member management,
 * island permissions, warps, and other properties.
 */
public class SkyblockManager {

    private static final Logger LOGGER = LogManager.getLogger(SkyblockManager.class);

    private static final int BLOCKS_PER_REGION = 512;
    private static final int REGION_HALF_SIZE = 256;

    private final Skyllia plugin;
    private final SkyblockCache cache;

    private final ConcurrentHashMap<Long, UUID> islandByRegion = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<UUID, Set<Long>> regionsByIsland = new ConcurrentHashMap<>();

    public SkyblockManager(Skyllia plugin, SkyblockCache cache) {
        this.plugin = plugin;
        this.cache = cache;
    }

    private static long pack(int x, int z) {
        return (((long) x) << 32) ^ (z & 0xffffffffL);
    }

    private static int chunkToRegion(int chunk) {
        return chunk >> 5;
    }

    private static int blockToRegion(int block) {
        return block >> 9;
    }

    private static int regionCenterBlock(int region) {
        return (region << 9) + REGION_HALF_SIZE;
    }

    private void unindexIsland(UUID islandId) {
        Set<Long> keys = regionsByIsland.remove(islandId);
        if (keys != null) {
            for (long k : keys) {
                islandByRegion.remove(k, islandId);
            }
        }
    }

    private void reindexIslandCoverage(Island island) {
        Position root = island.getPosition();
        if (root == null) return;

        int rootRx = root.x();
        int rootRz = root.z();

        int centerX = regionCenterBlock(rootRx);
        int centerZ = regionCenterBlock(rootRz);

        int radiusBlocks = (int) (island.getSize() / 2.0);

        int minRx = blockToRegion(centerX - radiusBlocks);
        int maxRx = blockToRegion(centerX + radiusBlocks);
        int minRz = blockToRegion(centerZ - radiusBlocks);
        int maxRz = blockToRegion(centerZ + radiusBlocks);

        UUID id = island.getId();

        unindexIsland(id);

        Set<Long> now = ConcurrentHashMap.newKeySet();
        for (int rx = minRx; rx <= maxRx; rx++) {
            for (int rz = minRz; rz <= maxRz; rz++) {
                long k = pack(rx, rz);
                islandByRegion.put(k, id);
                now.add(k);
            }
        }
        regionsByIsland.put(id, now);
    }

    private void cacheIslandAndIndex(Island island) {
        cache.putIsland(island);
        reindexIslandCoverage(island);
    }

    /**
     * Creates a new island with the specified {@link IslandSettings}.
     *
     * @param islandId   The UUID of the new island.
     * @param islandType The settings to apply to the new island.
     * @return A {@link CompletableFuture} that completes with {@code true} if the island was created,
     * or {@code false} if creation was cancelled or an error occurred.
     */
    public Boolean createIsland(UUID islandId, IslandSettings islandType) {
        PrepareSkyblockCreateEvent event = new PrepareSkyblockCreateEvent(islandId, islandType);
        Bukkit.getPluginManager().callEvent(event);

        // Check if the event was cancelled
        if (event.isCancelled()) {
            return false;
        }

        try {
            // Construct a new IslandHook instance before inserting it into the database
            Island futureIsland = new IslandHook(
                    event.getIslandId(),
                    event.getIslandSettings().maxMembers(),
                    null,
                    event.getIslandSettings().rayon(),
                    null
            );

            boolean success = plugin.getInterneAPI()
                    .getIslandQuery()
                    .getIslandDataQuery()
                    .insertIslands(futureIsland);

            if (success) {
                var permQuery = plugin.getInterneAPI()
                        .getIslandQuery()
                        .getIslandPermissionQuery();
                if (permQuery != null && ConfigLoader.permissionsV2 != null) {
                    String typeKey = event.getIslandSettings().name();

                    var blobs = ConfigLoader.permissionsV2.buildDefaultRoleBlobs(typeKey);
                    for (var entry : blobs.entrySet()) {
                        permQuery.saveRole(event.getIslandId(), entry.getKey(), entry.getValue());
                    }
                }

                invalidateIsland(event.getIslandId());
                return true;
            } else {
                LOGGER.fatal("Exception while creating a new island asynchronously");
                return false;
            }
        } catch (Exception e) {
            LOGGER.fatal("Exception before starting async creation", e);
            return false;
        }
    }

    public void invalidateIsland(UUID islandId) {
        cache.invalidateIsland(islandId);
        unindexIsland(islandId);
    }

    public @Nullable Players getOwnerByIslandId(UUID islandId) {
        Players cached = cache.getOwner(islandId);
        if (cached != null) return cached;

        Players owner = plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getOwnerByIslandId(islandId);
        if (owner != null) cache.putOwner(islandId, owner);
        return owner;
    }

    /**
     * Retrieves an island by its unique islandId.
     *
     * @param islandId The UUID of the island.
     * @return A {@link CompletableFuture} containing the {@link Island}, or {@code null} if not found.
     */
    public Island getIslandByIslandId(UUID islandId) {
        Island cached = cache.getIsland(islandId);
        if (cached != null) return cached;

        Island island = plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByIslandId(islandId);
        if (island != null) {
            cacheIslandAndIndex(island);
        }
        return island;
    }

    public List<Island> getAllIslandsValid() {
        return this.plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getAllIslandsValid();
    }

    /**
     * Disables or enables an island.
     *
     * @param island   The {@link Island} to update.
     * @param disabled {@code true} to disable, {@code false} to enable.
     * @return A {@link CompletableFuture} with {@code true} if successfully updated, {@code false} otherwise.
     */
    public Boolean disableIsland(Island island, boolean disabled) {
        boolean ok = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updateDisable(island, disabled);
        if (ok) {
            invalidateIsland(island.getId());
        }
        return ok;
    }

    /**
     * Checks if an island is currently disabled.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with {@code true} if disabled, {@code false} otherwise.
     */
    public Boolean isDisabledIsland(Island island) {
        SkyblockCache.IslandStateSnapshot state = cache.getState(island.getId());
        if (state != null) return state.disabled();

        boolean disabled = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isDisabledIsland(island);
        boolean priv = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isPrivateIsland(island);
        boolean locked = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isLockedIsland(island);
        int max = plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getMaxMemberInIsland(island);
        double size = island.getSize();

        cache.putState(island.getId(), new SkyblockCache.IslandStateSnapshot(disabled, priv, locked, max, size));
        return disabled;
    }

    /**
     * Sets whether an island is private.
     *
     * @param island        The {@link Island}.
     * @param privateIsland {@code true} for private, {@code false} for public.
     * @return A {@link CompletableFuture} with {@code true} if the state was successfully updated.
     */
    public Boolean setPrivateIsland(Island island, boolean privateIsland) {
        boolean ok = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().updatePrivate(island, privateIsland);
        if (ok) {
            cache.invalidateState(island.getId());
        }
        return ok;
    }

    /**
     * Checks if an island is private.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with {@code true} if private, {@code false} otherwise.
     */
    public Boolean isPrivateIsland(Island island) {
        SkyblockCache.IslandStateSnapshot state = cache.getState(island.getId());
        if (state != null) return state.priv();

        boolean disabled = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isDisabledIsland(island);
        boolean priv = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isPrivateIsland(island);
        boolean locked = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isLockedIsland(island);
        int max = plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getMaxMemberInIsland(island);
        double size = island.getSize();

        cache.putState(island.getId(), new SkyblockCache.IslandStateSnapshot(disabled, priv, locked, max, size));
        return priv;
    }

    public @Nullable Island getIslandByPosition(Position position) {
        if (position == null) return null;

        long key = pack(position.x(), position.z());
        UUID islandId = islandByRegion.get(key);

        if (islandId != null) {
            Island cached = cache.getIsland(islandId);
            if (cached != null) {
                return cached;
            }

            Island fromDbById = plugin.getInterneAPI()
                    .getIslandQuery()
                    .getIslandDataQuery()
                    .getIslandByIslandId(islandId);

            if (fromDbById != null) {
                cacheIslandAndIndex(fromDbById);
                return fromDbById;
            } else {
                // index stale
                islandByRegion.remove(key, islandId);
            }
        }

        Island island = plugin.getInterneAPI()
                .getIslandQuery()
                .getIslandDataQuery()
                .getIslandByPosition(position);

        if (island != null) cacheIslandAndIndex(island);
        return island;
    }

    private int regionRadiusFromConfig() {
        int d = ConfigLoader.general.getRegionDistance();
        if (d <= 0) return 1;
        int maxRadiusBlocks = d / 2;
        return Math.max(1, (int) Math.ceil(maxRadiusBlocks / 512.0));
    }

    public @Nullable Island getIslandByChunk(Chunk chunk) {
        if (chunk == null) return null;
        return getIslandByChunk(chunk.getX(), chunk.getZ());
    }

    public @Nullable Island getIslandByChunk(int chunkX, int chunkZ) {
        int rx = chunkToRegion(chunkX);
        int rz = chunkToRegion(chunkZ);
        return getIslandByPosition(new Position(rx, rz));
    }

    /**
     * Retrieves the island owned by a specific player.
     *
     * @param playerId The player's UUID.
     * @return A {@link CompletableFuture} that completes with the {@link Island}, or {@code null} if none.
     */
    public @Nullable Island getIslandByOwner(UUID playerId) {
        UUID cachedIslandId = cache.getIslandIdByPlayer(playerId);
        if (cachedIslandId != null) {
            Island island = cache.getIsland(cachedIslandId);
            if (island != null) return island;
        }

        Island island = plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByOwnerId(playerId);
        if (island != null) {
            cache.putIslandIdByPlayer(playerId, island.getId());
            cacheIslandAndIndex(island);
        }
        return island;
    }

    /**
     * Retrieves any island the player is a member of (not necessarily the owner).
     *
     * @param playerId The player's UUID.
     * @return A {@link CompletableFuture} that completes with the {@link Island}, or {@code null} if none.
     */
    public @Nullable Island getIslandByPlayerId(UUID playerId) {
        UUID cachedIslandId = cache.getIslandIdByPlayer(playerId);
        if (cachedIslandId != null) {
            Island cachedIsland = cache.getIsland(cachedIslandId);
            if (cachedIsland != null) return cachedIsland;
        }

        Island island = plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByPlayerId(playerId);
        if (island != null) {
            cache.putIslandIdByPlayer(playerId, island.getId());
            cacheIslandAndIndex(island);
        }
        return island;
    }

    /**
     * Adds a warp to an island.
     *
     * @param islandId       The UUID of the island.
     * @param name           The name of the warp.
     * @param playerLocation The warp location.
     */
    public Boolean addWarpsIsland(UUID islandId, String name, Location playerLocation) {
        boolean ok = plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().updateWarp(islandId, name, playerLocation);
        if (ok) {
            cache.invalidateWarps(islandId);
        }
        return ok;
    }

    /**
     * Deletes a warp from an island.
     *
     * @param islandId The UUID of the island.
     * @param name     The warp name.
     */
    public Boolean delWarpsIsland(UUID islandId, String name) {
        boolean ok = plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().deleteWarp(islandId, name);
        if (ok) {
            cache.invalidateWarps(islandId);
        }
        return ok;
    }

    /**
     * Retrieves a warp by its name from an island.
     *
     * @param islandId The UUID of the island.
     * @param name     The warp name.
     */
    public @Nullable WarpIsland getWarpIslandByName(UUID islandId, String name) {
        WarpIsland cached = cache.getWarp(islandId, name);
        if (cached != null) return cached;

        WarpIsland warp = plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().getWarpByName(islandId, name);
        if (warp != null) cache.putWarp(islandId, name, warp);
        return warp;
    }

    /**
     * Retrieves all warps for an island.
     *
     * @param islandId The UUID of the island.
     */
    public @Nullable List<WarpIsland> getWarpsIsland(UUID islandId) {
        List<WarpIsland> cached = cache.getWarps(islandId);
        if (cached != null) return cached;

        List<WarpIsland> warps = plugin.getInterneAPI().getIslandQuery().getIslandWarpQuery().getListWarp(islandId);
        if (warps != null) cache.putWarps(islandId, warps);
        return warps;
    }

    /**
     * Updates or adds a member on the specified island.
     *
     * @param island  The {@link Island}.
     * @param players The {@link Players} object representing the member.
     */
    public Boolean updateMember(Island island, Players players) {
        boolean ok = plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().updateMember(island, players);
        if (ok) {
            cache.invalidateMembers(island.getId());
            cache.invalidatePlayerLink(players.getMojangId());
            island.invalidateCompiledPermissions();
        }
        return ok;
    }

    /**
     * Retrieves all members on the specified island.
     *
     * @param island The {@link Island}.
     */
    public List<Players> getMembersInIsland(Island island) {
        List<Players> cached = cache.getMembers(island.getId());
        if (cached != null) return cached;

        List<Players> members = plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getMembersInIsland(island);
        if (members != null) cache.putMembers(island.getId(), members);
        return members != null ? members : List.of();
    }

    public List<Players> getBannedMembersInIsland(Island island) {
        List<Players> cached = cache.getBanned(island.getId());
        if (cached != null) return cached;

        List<Players> banned = plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().getBannedMembersInIsland(island);
        if (banned != null) cache.putBanned(island.getId(), banned);
        return banned != null ? banned : List.of();
    }

    /**
     * Gets a specific member by their UUID from the island.
     *
     * @param island   The {@link Island}.
     * @param playerId The player's UUID.
     */
    public Players getMemberInIsland(Island island, UUID playerId) {
        UUID islandId = island.getId();
        var cachedRole = cache.getRole(islandId, playerId);
        if (cachedRole != null) {
            if (cachedRole == RoleType.VISITOR) return null;
            List<Players> members = cache.getMembers(islandId);
            if (members != null) {
                for (Players p : members) {
                    if (p.getMojangId().equals(playerId)) return p;
                }
            }
            return new Players(playerId, "", islandId, cachedRole);
        }

        List<Players> members = cache.getMembers(islandId);
        if (members != null) {
            for (Players p : members) {
                if (p.getMojangId().equals(playerId)) {
                    cache.putRole(islandId, playerId, p.getRoleType());
                    return p;
                }
            }
            cache.putRole(islandId, playerId, RoleType.VISITOR);
            return null;
        }
        Players fromDb = plugin.getInterneAPI()
                .getIslandQuery()
                .getIslandMemberQuery()
                .getPlayersIsland(island, playerId);
        if (fromDb != null) {
            cache.putRole(islandId, playerId, fromDb.getRoleType());
            return fromDb;
        }
        cache.putRole(islandId, playerId, RoleType.VISITOR);
        return null;
    }

    /**
     * Gets a specific member by name from the island.
     *
     * @param island     The {@link Island}.
     * @param playerName The player's name.
     */
    public @Nullable Players getMemberInIsland(Island island, String playerName) {
        if (playerName == null || playerName.isBlank()) return null;

        UUID islandId = island.getId();
        RoleType cached = cache.getRoleByName(islandId, playerName);
        if (cached != null) {
            if (cached == RoleType.VISITOR) return null;

            List<Players> members = cache.getMembers(islandId);
            if (members != null) {
                for (Players p : members) {
                    if (p.getLastKnowName() != null
                            && p.getLastKnowName().equalsIgnoreCase(playerName)) {
                        return p;
                    }
                }
            }

            return new Players(null, playerName, islandId, cached);
        }

        List<Players> members = cache.getMembers(islandId);
        if (members != null) {
            for (Players p : members) {
                if (p.getLastKnowName() != null && p.getLastKnowName().equalsIgnoreCase(playerName)) {
                    cache.putRole(islandId, p.getMojangId(), p.getRoleType());
                    cache.putRoleByName(islandId, playerName, p.getRoleType());
                    return p;
                }
            }
            cache.putRoleByName(islandId, playerName, RoleType.VISITOR);
            return null;
        }

        Players fromDb = plugin.getInterneAPI()
                .getIslandQuery()
                .getIslandMemberQuery()
                .getPlayersIsland(island, playerName);

        if (fromDb != null) {
            cache.putRole(islandId, fromDb.getMojangId(), fromDb.getRoleType());
            if (fromDb.getLastKnowName() != null) {
                cache.putRoleByName(islandId, fromDb.getLastKnowName().toLowerCase(java.util.Locale.ROOT), fromDb.getRoleType());
            }
            return fromDb;
        }

        cache.putRoleByName(islandId, playerName, RoleType.VISITOR);
        return null;
    }

    /**
     * Adds a record to clear a member on their next login.
     *
     * @param playerId The player's UUID.
     * @param cause    The {@link RemovalCause}.
     * @return A {@link CompletableFuture} with {@code true} if successful, {@code false} otherwise.
     */
    public Boolean addClearMemberNextLogin(UUID playerId, RemovalCause cause) {
        return plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().addMemberClear(playerId, cause);
    }

    /**
     * Deletes a scheduled member clear record.
     *
     * @param playerId The player's UUID.
     * @param cause    The {@link RemovalCause}.
     * @return A {@link CompletableFuture} with {@code true} if deleted, {@code false} otherwise.
     */
    public Boolean deleteClearMember(UUID playerId, RemovalCause cause) {
        return plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().deleteMemberClear(playerId, cause);
    }

    /**
     * Checks if a clear member record exists.
     *
     * @param playerId The player's UUID.
     * @param cause    The {@link RemovalCause}.
     * @return A {@link CompletableFuture} with {@code true} if exists, {@code false} otherwise.
     */
    public Boolean checkClearMemberExist(UUID playerId, RemovalCause cause) {
        return plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().checkClearMemberExist(playerId, cause);
    }

    /**
     * Deletes a member from the island.
     *
     * @param island    The {@link Island}.
     * @param oldMember The {@link Players} object representing the member to delete.
     * @return A {@link CompletableFuture} with {@code true} if deleted, {@code false} otherwise.
     */
    public Boolean deleteMember(Island island, Players oldMember) {
        boolean ok = plugin.getInterneAPI().getIslandQuery().getIslandMemberQuery().deleteMember(island, oldMember);
        if (ok) {
            cache.invalidateMembers(island.getId());
            cache.invalidatePlayerLink(oldMember.getMojangId());
            island.invalidateCompiledPermissions();
        }
        return ok;
    }

    /**
     * Retrieves the owner of the island.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with the {@link Players} object of the owner, or null if none.
     */
    public @Nullable Players getOwnerByIslandID(Island island) {
        return getOwnerByIslandId(island.getId());
    }

    /**
     * Retrieves the maximum number of members allowed in the island.
     *
     * @param island The {@link Island}.
     * @return A {@link CompletableFuture} with the max member count, or -1 if not found.
     */
    public Integer getMaxMemberInIsland(Island island) {
        SkyblockCache.IslandStateSnapshot state = cache.getState(island.getId());
        if (state != null) return state.maxMembers();

        int max = plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getMaxMemberInIsland(island);
        cache.invalidateState(island.getId());
        return max;
    }

    /**
     * Sets the maximum number of members allowed in the island.
     *
     * @param island   The {@link Island}.
     * @param newValue The new maximum.
     * @return A {@link CompletableFuture} with {@code true} if successfully updated, {@code false} otherwise.
     */
    public Boolean setMaxMemberInIsland(Island island, int newValue) {
        boolean ok = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().setMaxMemberInIsland(island, newValue);
        if (ok) {
            cache.invalidateState(island.getId());
        }
        return ok;
    }

    /**
     * Sets the size of the island.
     *
     * @param island   The {@link Island}.
     * @param newValue The new size (radius).
     * @return A {@link CompletableFuture} with {@code true} if successfully updated, {@code false} otherwise.
     */
    public Boolean setSizeIsland(Island island, double newValue) {
        boolean ok = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().setSizeIsland(island, newValue);
        if (!ok) return false;
        cache.invalidateState(island.getId());

        Island reloaded = plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getIslandByIslandId(island.getId());
        if (reloaded != null) cacheIslandAndIndex(reloaded);
        else invalidateIsland(island.getId());

        return true;
    }

    /**
     * Met à jour l'état "locked" de l'île (vaut true pendant un delete).
     */
    public Boolean setLockedIsland(Island island, boolean locked) {
        boolean ok = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().setLockedIsland(island, locked);
        if (ok) {
            cache.invalidateState(island.getId());
        }
        return ok;
    }

    /**
     * Vérifie si l'île est actuellement "locked".
     */
    public Boolean isLockedIsland(Island island) {
        SkyblockCache.IslandStateSnapshot state = cache.getState(island.getId());
        if (state != null) return state.locked();

        boolean disabled = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isDisabledIsland(island);
        boolean priv = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isPrivateIsland(island);
        boolean locked = plugin.getInterneAPI().getIslandQuery().getIslandUpdateQuery().isLockedIsland(island);
        int max = plugin.getInterneAPI().getIslandQuery().getIslandDataQuery().getMaxMemberInIsland(island);
        double size = island.getSize();

        cache.putState(island.getId(), new SkyblockCache.IslandStateSnapshot(disabled, priv, locked, max, size));
        return locked;
    }

}
