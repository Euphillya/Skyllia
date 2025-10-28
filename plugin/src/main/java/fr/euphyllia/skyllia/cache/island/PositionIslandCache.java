package fr.euphyllia.skyllia.cache.island;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.CacheStatsMonitor;

import java.util.concurrent.TimeUnit;

public class PositionIslandCache {

    private static final Cache<Position, Island> POSITION_CACHE = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(50000)
            .recordStats()
            .build();

    static {
        CacheStatsMonitor.registerCache("PositionIslandCache", POSITION_CACHE::estimatedSize, POSITION_CACHE::stats);
    }

    public static Island getIsland(Position pos) {
        return POSITION_CACHE.getIfPresent(pos);
    }

    public static void putIsland(Position pos, Island island) {
        POSITION_CACHE.put(pos, island);
    }

    public static void remove(Position pos) {
        POSITION_CACHE.invalidate(pos);
    }

    public static void invalidateAll() {
        POSITION_CACHE.invalidateAll();
    }

    public static void updateIslandPositions(Island island) {
        var positions = RegionHelper.getRegionsWithinBlockRange(island.getPosition(), (int) Math.round(island.getSize()));
        var updates = new java.util.HashMap<Position, Island>(positions.size());
        positions.forEach(pos -> updates.put(pos, island));
        POSITION_CACHE.putAll(updates);
    }
}