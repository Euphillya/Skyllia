package fr.euphyllia.skyllia.cache.rules;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PermissionGameRuleInIslandCache {

    private static SkyblockManager skyblockManager;

    public static void init(SkyblockManager manager) {
        skyblockManager = manager;
    }

    public static long getGameRule(UUID islandId) {
        return -1;
    }

    public static void invalidateGameRule(UUID islandId) {

    }

    public static void invalidateAll() {

    }
}