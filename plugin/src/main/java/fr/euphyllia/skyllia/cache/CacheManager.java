package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CacheManager {

    private final Logger logger = LogManager.getLogger(CacheManager.class);
    private final SkyblockManager skyblockManager;
    private final InterneAPI api;

    public CacheManager(SkyblockManager skyblockManager, InterneAPI interneAPI) {
        this.skyblockManager = skyblockManager;
        this.api = interneAPI;
    }

    public void updateCache(SkyblockManager skyblockManager, Player bPlayer) {
        Island pIsland = skyblockManager.getIslandByPlayerId(bPlayer.getUniqueId()).join();
        if (pIsland == null) {
            // ========= remove player
            PlayersInIslandCache.getIslandIdByPlayerId().remove(bPlayer.getUniqueId());
            PlayersInIslandCache.getIslandByPlayerId().remove(bPlayer.getUniqueId());
            return;
        }
        this.updateCacheIsland(pIsland, bPlayer.getUniqueId());
    }

    public void deleteCacheIsland(Island island) {
        Bukkit.getAsyncScheduler().runNow(api.getPlugin(), task -> {
            UUID islandId = island.getId();
            // ============= player cache
            for (Players players : island.getMembers()) {
                PlayersInIslandCache.getIslandIdByPlayerId().remove(players.getMojangId(), islandId);
                PlayersInIslandCache.getIslandByPlayerId().remove(players.getMojangId());
                PlayersInIslandCache.getIslandByIslandId().remove(islandId);
            }

            PlayersInIslandCache.delete(islandId);
            // ============= position island cache
            List<Position> islandPositionWithRadius = RegionHelper.getRegionsInRadius(island.getPosition(), (int) Math.round(island.getSize()));
            for (Position possiblePosition : islandPositionWithRadius) {
                PositionIslandCache.delete(possiblePosition);
            }
            // ============= permission role cache
            for (RoleType roleType : RoleType.values()) {
                for (PermissionsType permissionsType : PermissionsType.values()) {
                    PermissionRoleInIslandCache.deletePermissionInIsland(islandId, roleType, permissionsType);
                }
            }
            // =========== supprimer cache gamerule
            PermissionGameRuleInIslandCache.deleteGameruleByIslandId(islandId);
        });
    }

    public void updateCacheIsland(Island island, UUID playerId) {
        Bukkit.getAsyncScheduler().runNow(api.getPlugin(), task -> {
            PlayersInIslandCache.getIslandIdByPlayerId().put(playerId, island.getId());
            PlayersInIslandCache.getIslandByPlayerId().put(playerId, island);
            PlayersInIslandCache.add(island.getId(), island.getMembers());
            PlayersInIslandCache.getIslandByIslandId().put(island.getId(), island);
            // ============= position island cache
            List<Position> islandPositionWithRadius = RegionHelper.getRegionsInRadius(island.getPosition(), (int) Math.round(island.getSize()));
            for (Position possiblePosition : islandPositionWithRadius) {
                PositionIslandCache.add(possiblePosition, island);
            }
            // ============= permission role cache
            this.updatePermissionCacheIsland(island);
            // ============= gamerule cache
            this.updateGameRuleCacheIsland(island);
        });
    }

    public void updatePermissionCacheIsland(Island island) {
        for (RoleType roleType : RoleType.values()) {
            for (PermissionsType permissionsType : PermissionsType.values()) {
                PermissionRoleIsland permissionRoleIsland = skyblockManager.getPermissionIsland(island.getId(), permissionsType, roleType).join();
                PermissionRoleInIslandCache.addPermissionInIsland(island.getId(), roleType, permissionsType, permissionRoleIsland);
            }
        }
    }

    public void updateGameRuleCacheIsland(Island island) {
        long valueGR = island.getGameRulePermission();
        PermissionGameRuleInIslandCache.setGameruleByIslandId(island.getId(), valueGR);
    }
}
