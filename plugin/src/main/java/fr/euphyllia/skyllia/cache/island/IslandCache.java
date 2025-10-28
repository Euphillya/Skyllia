package fr.euphyllia.skyllia.cache.island;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class IslandCache {

    private static final LoadingCache<UUID, Island> ISLAND_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(10000)
            .build(IslandCache::loadIsland);

    public static Island getIsland(UUID islandId) {
        return ISLAND_CACHE.get(islandId);
    }

    public static void invalidateIsland(UUID islandId) {
        ISLAND_CACHE.invalidate(islandId);
    }

    public static void invalidateAll() {
        ISLAND_CACHE.invalidateAll();
    }

    private static Island loadIsland(UUID islandId) {
        return SkylliaAPI.getIslandByIslandId(islandId).join();
    }
}