package fr.euphyllia.skyllia.cache.island;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class PlayersInIslandCache {

    private static final Logger logger = LogManager.getLogger(PlayersInIslandCache.class);

    /**
     * islandId -> list of Players
     */
    private static final LoadingCache<UUID, CopyOnWriteArrayList<Players>> listPlayersInIsland =
            Caffeine.newBuilder()
                    .expireAfterAccess(15, TimeUnit.MINUTES)
                    .build(PlayersInIslandCache::loadPlayersFromIslandCache);

    /**
     * playerId -> islandId
     */
    private static final Cache<UUID, UUID> islandIdByPlayerId =
            Caffeine.newBuilder()
                    .expireAfterAccess(15, TimeUnit.MINUTES)
                    .build();

    /**
     * islandId -> list of trusted players
     */
    private static final Cache<UUID, CopyOnWriteArrayList<UUID>> listTrustedPlayerByIslandId =
            Caffeine.newBuilder()
                    .expireAfterAccess(15, TimeUnit.MINUTES)
                    .build();


    // =====================================================================
    // 1) Méthodes pour la liste de joueurs
    // =====================================================================

    private static CopyOnWriteArrayList<Players> loadPlayersFromIslandCache(UUID islandId) {
        Island island = IslandCache.getIsland(islandId);
        if (island == null) {
            return new CopyOnWriteArrayList<>();
        }
        return new CopyOnWriteArrayList<>(island.getMembers());
    }

    public static CopyOnWriteArrayList<Players> getPlayersCached(UUID islandId) {
        return listPlayersInIsland.get(islandId);
    }

    public static Players getPlayers(UUID islandId, UUID playerId) {
        List<Players> playersInIsland = getPlayersCached(islandId);
        if (playersInIsland.isEmpty()) {
            return new Players(playerId, null, islandId, RoleType.VISITOR);
        }
        for (Players players : playersInIsland) {
            if (players.getMojangId().equals(playerId)) {
                return players;
            }
        }
        return new Players(playerId, null, islandId, RoleType.VISITOR);
    }

    public static void delete(UUID islandId) {
        listPlayersInIsland.invalidate(islandId);
    }

    public static void add(UUID islandId, CopyOnWriteArrayList<Players> members) {
        listPlayersInIsland.put(islandId, members);
    }

    // =====================================================================
    // 2) Méthodes pour islandIdByPlayerId
    // =====================================================================

    public static UUID getIslandIdByPlayer(UUID playerId) {
        return islandIdByPlayerId.getIfPresent(playerId);
    }

    public static void setIslandIdByPlayer(UUID playerId, UUID islandId) {
        islandIdByPlayerId.put(playerId, islandId);
    }

    public static void removeIslandForPlayer(UUID playerId) {
        islandIdByPlayerId.invalidate(playerId);
    }

    // =====================================================================
    // 3) Méthodes pour listTrustedPlayerByIslandId
    // =====================================================================

    public static CopyOnWriteArrayList<UUID> getPlayersListTrusted(UUID islandId) {
        return listTrustedPlayerByIslandId.get(islandId, k -> new CopyOnWriteArrayList<>());
    }

    public static void addPlayerTrustedInIsland(UUID islandId, UUID playerId) {
        listTrustedPlayerByIslandId.asMap().compute(islandId, (key, oldList) -> {
            if (oldList == null) {
                oldList = new CopyOnWriteArrayList<>();
            }
            oldList.add(playerId);
            return oldList;
        });
    }

    public static boolean removePlayerTrustedInIsland(UUID islandId, UUID playerId) {
        var listPlayer = listTrustedPlayerByIslandId.getIfPresent(islandId);
        if (listPlayer == null) return false;
        boolean isRemoved = listPlayer.remove(playerId);
        if (listPlayer.isEmpty()) {
            listTrustedPlayerByIslandId.invalidate(islandId);
        }
        return isRemoved;
    }

    public static boolean playerIsTrustedInIsland(UUID islandId, UUID playerId) {
        var list = getPlayersListTrusted(islandId);
        return list.contains(playerId);
    }

    public static void invalidateAll() {
        listPlayersInIsland.invalidateAll();
        islandIdByPlayerId.invalidateAll();
        listTrustedPlayerByIslandId.invalidateAll();
    }
}