package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CacheIsland {

    private static final ConcurrentHashMap<UUID, Boolean> islandClosed = new ConcurrentHashMap<>();

    public static Boolean getIslandClosed(UUID islandId) {
        if (islandClosed.get(islandId) == null) {
            Island island = SkylliaAPI.getCacheIslandByIslandId(islandId);
            if (island == null) return false;
            boolean access = island.isPrivateIsland();
            islandClosed.put(islandId, access);
            return access;
        } else {
            return islandClosed.get(islandId);
        }
    }
}
