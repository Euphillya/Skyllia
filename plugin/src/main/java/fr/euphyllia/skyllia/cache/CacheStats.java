package fr.euphyllia.skyllia.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Utility class for monitoring cache statistics across the plugin.
 * Uses a registry pattern to avoid reflection.
 */
public class CacheStatsMonitor {

    private static final Logger logger = LogManager.getLogger(CacheStatsMonitor.class);
    private static final Map<String, CacheStatsSupplier> CACHE_REGISTRY = new ConcurrentHashMap<>();

    /**
     * Registers a cache with the stats monitor.
     * 
     * @param name The name of the cache
     * @param sizeSupplier Supplier for the cache size
     * @param statsSupplier Supplier for the cache stats
     */
    public static void registerCache(String name, Supplier<Long> sizeSupplier, Supplier<CacheStats> statsSupplier) {
        CACHE_REGISTRY.put(name, new CacheStatsSupplier(sizeSupplier, statsSupplier));
    }

    /**
     * Collects statistics from all registered caches.
     * 
     * @return A map of cache names to their statistics
     */
    public static Map<String, CacheStatEntry> collectAllStats() {
        Map<String, CacheStatEntry> stats = new LinkedHashMap<>();
        
        for (Map.Entry<String, CacheStatsSupplier> entry : CACHE_REGISTRY.entrySet()) {
            try {
                CacheStatsSupplier supplier = entry.getValue();
                long size = supplier.sizeSupplier().get();
                CacheStats cacheStats = supplier.statsSupplier().get();
                stats.put(entry.getKey(), new CacheStatEntry(size, cacheStats.hitRate(), cacheStats.evictionCount()));
            } catch (Exception e) {
                logger.error("Failed to collect stats for cache: {}", entry.getKey(), e);
            }
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

    private record CacheStatsSupplier(Supplier<Long> sizeSupplier, Supplier<CacheStats> statsSupplier) {}

    public record CacheStatEntry(long size, double hitRate, long evictionCount) {}
}
