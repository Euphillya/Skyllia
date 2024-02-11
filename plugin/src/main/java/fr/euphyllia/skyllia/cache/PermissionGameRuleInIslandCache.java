package fr.euphyllia.skyllia.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionGameRuleInIslandCache {
    private static final Logger logger = LogManager.getLogger(PermissionGameRuleInIslandCache.class);
    private static ConcurrentHashMap<UUID, Long> gameruleByIslandId = new ConcurrentHashMap<>();


    public static void setGameruleByIslandId(UUID islandId, Long permission) {
        gameruleByIslandId.put(islandId, permission);
    }

    public static Long getGameruleInIsland(UUID islandId) {
        return gameruleByIslandId.getOrDefault(islandId, -1L);
    }

    public static void deleteGameruleByIslandId(UUID islandId) {
        gameruleByIslandId.remove(islandId);
    }
}

