package fr.euphyllia.skyllia.cache.island;

import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class WarpsInIslandCache {

    private static final Logger logger = LogManager.getLogger(WarpsInIslandCache.class);

    private static final LoadingCache<UUID, CopyOnWriteArrayList<WarpIsland>> WARPS_CACHE =
            Caffeine.newBuilder()
                    .expireAfterAccess(15, TimeUnit.MINUTES)
                    .refreshAfterWrite(10, TimeUnit.MINUTES)
                    .build(WarpsInIslandCache::loadWarpsFromDB);

    private static CopyOnWriteArrayList<WarpIsland> loadWarpsFromDB(UUID islandId) {
        try {
            SkyblockManager manager = Skyllia.getPlugin(Skyllia.class).getInterneAPI().getSkyblockManager();
            CopyOnWriteArrayList<WarpIsland> warps = manager.getWarpsIsland(islandId).join();
            return warps != null ? warps : new CopyOnWriteArrayList<>();
        } catch (Exception e) {
            logger.error("Failed to load warps for island {}", islandId, e);
            return new CopyOnWriteArrayList<>();
        }
    }

    public static CopyOnWriteArrayList<WarpIsland> getWarpsCached(UUID islandId) {
        return WARPS_CACHE.get(islandId);
    }

    public static void setWarps(UUID islandId, CopyOnWriteArrayList<WarpIsland> warps) {
        WARPS_CACHE.put(islandId, warps);
    }

    public static void invalidate(UUID islandId) {
        WARPS_CACHE.invalidate(islandId);
    }

    public static void invalidateAll() {
        WARPS_CACHE.invalidateAll();
    }
}
