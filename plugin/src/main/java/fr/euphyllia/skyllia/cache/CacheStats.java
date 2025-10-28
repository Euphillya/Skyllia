package fr.euphyllia.skyllia.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.euphyllia.skyllia.cache.commands.CommandCacheExecution;
import fr.euphyllia.skyllia.cache.commands.InviteCacheExecution;
import fr.euphyllia.skyllia.cache.island.*;
import fr.euphyllia.skyllia.cache.rules.PermissionGameRuleInIslandCache;
import fr.euphyllia.skyllia.cache.rules.PermissionRoleInIslandCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class for monitoring cache statistics across the plugin.
 */
public class CacheStats {

    private static final Logger logger = LogManager.getLogger(CacheStats.class);

    /**
     * Collects statistics from all caches in the plugin.
     * 
     * @return A map of cache names to their statistics
     */
    public static Map<String, CacheStatEntry> collectAllStats() {
        Map<String, CacheStatEntry> stats = new LinkedHashMap<>();
        
        try {
            // Island caches
            addCacheStats(stats, "IslandCache", getFieldValue(IslandCache.class, "ISLAND_CACHE"));
            addCacheStats(stats, "PlayersInIslandCache.listPlayersInIsland", getFieldValue(PlayersInIslandCache.class, "listPlayersInIsland"));
            addCacheStats(stats, "PlayersInIslandCache.islandIdByPlayerId", getFieldValue(PlayersInIslandCache.class, "islandIdByPlayerId"));
            addCacheStats(stats, "PlayersInIslandCache.listTrustedPlayerByIslandId", getFieldValue(PlayersInIslandCache.class, "listTrustedPlayerByIslandId"));
            addCacheStats(stats, "PositionIslandCache", getFieldValue(PositionIslandCache.class, "POSITION_CACHE"));
            addCacheStats(stats, "WarpsInIslandCache", getFieldValue(WarpsInIslandCache.class, "WARPS_CACHE"));
            addCacheStats(stats, "IslandClosedCache", getFieldValue(IslandClosedCache.class, "ISLAND_CLOSED_CACHE"));
            
            // Rules caches
            addCacheStats(stats, "PermissionRoleInIslandCache", getFieldValue(PermissionRoleInIslandCache.class, "PERMISSION_ROLE_CACHE"));
            addCacheStats(stats, "PermissionGameRuleInIslandCache", getFieldValue(PermissionGameRuleInIslandCache.class, "GAMERULE_CACHE"));
            
            // Command caches
            addCacheStats(stats, "CommandCacheExecution", getFieldValue(CommandCacheExecution.class, "COMMAND_CACHE"));
            addCacheStats(stats, "InviteCacheExecution", getFieldValue(InviteCacheExecution.class, "INVITE_CACHE"));
            
        } catch (Exception e) {
            logger.error("Failed to collect cache statistics", e);
        }
        
        return stats;
    }

    /**
     * Logs cache statistics to the logger.
     */
    public static void logStats() {
        Map<String, CacheStatEntry> stats = collectAllStats();
        logger.info("=== Cache Statistics ===");
        for (Map.Entry<String, CacheStatEntry> entry : stats.entrySet()) {
            CacheStatEntry stat = entry.getValue();
            logger.info("{}: size={}, hitRate={:.2f}%, evictions={}", 
                entry.getKey(), 
                stat.size, 
                stat.hitRate * 100, 
                stat.evictionCount);
        }
        logger.info("========================");
    }

    private static Object getFieldValue(Class<?> clazz, String fieldName) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    private static void addCacheStats(Map<String, CacheStatEntry> statsMap, String name, Object cache) {
        if (cache instanceof LoadingCache<?, ?> loadingCache) {
            long size = loadingCache.estimatedSize();
            com.github.benmanes.caffeine.cache.stats.CacheStats stats = loadingCache.stats();
            statsMap.put(name, new CacheStatEntry(size, stats.hitRate(), stats.evictionCount()));
        } else if (cache instanceof Cache<?, ?> regularCache) {
            long size = regularCache.estimatedSize();
            com.github.benmanes.caffeine.cache.stats.CacheStats stats = regularCache.stats();
            statsMap.put(name, new CacheStatEntry(size, stats.hitRate(), stats.evictionCount()));
        }
    }

    public record CacheStatEntry(long size, double hitRate, long evictionCount) {}
}
