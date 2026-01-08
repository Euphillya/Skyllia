package fr.euphyllia.skyllia.cache.rules;

import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;

import java.util.UUID;

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