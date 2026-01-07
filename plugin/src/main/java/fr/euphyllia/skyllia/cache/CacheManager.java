package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.cache.commands.CommandCacheExecution;
import fr.euphyllia.skyllia.cache.commands.InviteCacheExecution;
import fr.euphyllia.skyllia.cache.island.*;
import fr.euphyllia.skyllia.cache.rules.PermissionGameRuleInIslandCache;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CacheManager {

    private static final Logger logger = LogManager.getLogger(CacheManager.class);

    private final SkyblockManager skyblockManager;
    private final InterneAPI api;

    public CacheManager(SkyblockManager skyblockManager, InterneAPI interneAPI) {
        this.skyblockManager = skyblockManager;
        this.api = interneAPI;

//        PermissionRoleInIslandCache.init(skyblockManager);
        PermissionGameRuleInIslandCache.init(skyblockManager);
    }

    public void updateCache(Player bPlayer) {
        Island island = skyblockManager.getIslandByPlayerId(bPlayer.getUniqueId());
        if (island == null) {
            PlayersInIslandCache.removeIslandForPlayer(bPlayer.getUniqueId());
            return;
        }

        PlayersInIslandCache.setIslandIdByPlayer(bPlayer.getUniqueId(), island.getId());

        IslandCache.getIsland(island.getId());

        PlayersInIslandCache.getPlayersCached(island.getId());
    }

    public void updateCacheIsland(Island island) {
        UUID islandId = island.getId();
        // Invalide l'île dans le IslandCache
        IslandCache.invalidateIsland(islandId);

        PlayersInIslandCache.delete(islandId);

        PermissionGameRuleInIslandCache.invalidateGameRule(islandId);

        PositionIslandCache.updateIslandPositions(island);

        Island reloadedIsland = IslandCache.getIsland(islandId);
        if (reloadedIsland != null) {
            PlayersInIslandCache.getPlayersCached(islandId);

//            for (RoleType roleType : RoleType.values()) {
//                for (PermissionsType permissionsType : PermissionsType.values()) {
//                    PermissionRoleInIslandCache.getPermissionRoleIsland(islandId, roleType, permissionsType);
//                }
//            }

            PermissionGameRuleInIslandCache.getGameRule(islandId);

            PositionIslandCache.updateIslandPositions(reloadedIsland);
        }
    }

    public void deleteCacheIsland(Island island) {
        UUID islandId = island.getId();

        IslandCache.invalidateIsland(islandId);

        PlayersInIslandCache.delete(islandId);

        for (var member : island.getMembers()) {
            PlayersInIslandCache.removeIslandForPlayer(member.getMojangId());
        }

        PermissionGameRuleInIslandCache.invalidateGameRule(islandId);

        logger.debug("All caches invalidated for deleted island {}", islandId);
    }

    public void invalidateAll() {
        // Commands
        //CacheCommands.invalidateAll();
        CommandCacheExecution.invalidateAll();
        InviteCacheExecution.invalidateAll();

        // Island
        IslandCache.invalidateAll();
        IslandClosedCache.invalidateAll();
        PlayersInIslandCache.invalidateAll();
        PositionIslandCache.invalidateAll();

        // Rules
        PermissionGameRuleInIslandCache.invalidateAll();
        WarpsInIslandCache.invalidateAll();
    }
}
