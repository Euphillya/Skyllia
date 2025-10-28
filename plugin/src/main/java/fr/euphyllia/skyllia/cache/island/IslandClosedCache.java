package fr.euphyllia.skyllia.cache.island;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.cache.CacheStatsMonitor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class IslandClosedCache {

    private static final LoadingCache<UUID, Boolean> ISLAND_CLOSED_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(10000)
            .recordStats()
            .build(IslandClosedCache::loadIslandClosed);

    static {
        CacheStatsMonitor.registerCache("IslandClosedCache", ISLAND_CLOSED_CACHE::estimatedSize, ISLAND_CLOSED_CACHE::stats);
    }


    public static boolean isIslandClosed(UUID islandId) {
        return ISLAND_CLOSED_CACHE.get(islandId);
    }

    public static void invalidateIsland(UUID islandId) {
        ISLAND_CLOSED_CACHE.invalidate(islandId);
    }

    private static Boolean loadIslandClosed(UUID islandId) {
        Island island = SkylliaAPI.getIslandByIslandId(islandId).join();
        if (island == null) {
            return false;
        }
        return island.isPrivateIsland();
    }

    public static void invalidateAll() {
        ISLAND_CLOSED_CACHE.invalidateAll();
    }
}