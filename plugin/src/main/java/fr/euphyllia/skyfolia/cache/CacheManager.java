package fr.euphyllia.skyfolia.cache;

import fr.euphyllia.skyfolia.api.annotation.Experimental;
import fr.euphyllia.skyfolia.api.annotation.Information;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyfolia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

@Experimental
@Information("Je n'ai pas tester encore cette fonctionnalité")
public class CacheManager {

    private final Logger logger = LogManager.getLogger(CacheManager.class);
    private final SkyblockManager skyblockManager;

    public CacheManager(SkyblockManager skyblockManager) {
        this.skyblockManager = skyblockManager;
    }

    public void updateCache(SkyblockManager skyblockManager, Player bPlayer) {
        Island pIsland = skyblockManager.getIslandByOwner(bPlayer.getUniqueId()).join();
        if (pIsland == null) {
            // ========= remove player
            PlayersInIslandCache.getIslandIdByPlayerId().remove(bPlayer.getUniqueId());
            return;
        }
        this.updateCacheIsland(pIsland, bPlayer.getUniqueId());
    }

    public void updateCacheIsland(Island island, UUID playerId) {
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            // ============= player cache
            PlayersInIslandCache.getIslandIdByPlayerId().put(playerId, island.getId());
            PlayersInIslandCache.getListPlayersInIsland().put(island.getId(), island.getMembers());
            // ============= position island cache
            PositionIslandCache.getPositionIslandId().put(island.getPosition().toString(), island.getId());
            // ============= permission role cache
            ConcurrentHashMap<RoleType, Integer> permissionByRole = new ConcurrentHashMap<>();
            for (PermissionsType permissionsType : PermissionsType.values()) {
                for (RoleType roleType : RoleType.values()) {
                    PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), permissionsType, roleType).join();
                    permissionByRole.put(roleType, permissionRoleIsland.permission());
                }
            }
            PermissionRoleInIslandCache.getListPermissionsInIsland().put(island.getId(), permissionByRole);

            logger.log(Level.INFO, island.getId() + " est mis à jour.");
        });
    }
}
