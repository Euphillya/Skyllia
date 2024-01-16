package fr.euphyllia.skyfolia.cache;

import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PermissionRoleInIslandCache {
    private static final Logger logger = LogManager.getLogger(PermissionRoleInIslandCache.class);
    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<RoleType, Integer>> listPermissionsInIsland = new ConcurrentHashMap<>();


    public static ConcurrentHashMap<UUID, ConcurrentHashMap<RoleType, Integer>> getListPermissionsInIsland() {
        return listPermissionsInIsland;
    }
}
