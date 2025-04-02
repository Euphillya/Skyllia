package fr.euphyllia.skyllia.cache.rules;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PermissionGameRuleInIslandCache {

    private static final LoadingCache<UUID, Long> GAMERULE_CACHE = Caffeine.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(PermissionGameRuleInIslandCache::loadGameRule);

    private static SkyblockManager skyblockManager;

    public static void init(SkyblockManager manager) {
        skyblockManager = manager;
    }

    public static long getGameRule(UUID islandId) {
        return GAMERULE_CACHE.get(islandId);
    }

    public static void invalidateGameRule(UUID islandId) {
        GAMERULE_CACHE.invalidate(islandId);
    }

    private static Long loadGameRule(UUID islandId) {
        Island island = SkylliaAPI.getIslandByIslandId(islandId).join();
        if (island == null) return -1L;
        return island.getGameRulePermission();
    }

    public static void invalidateAll() {
        GAMERULE_CACHE.invalidateAll();
    }
}