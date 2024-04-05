package fr.euphyllia.skyllia.listeners;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.event.players.PlayerPrepareChangeWorldSkyblockEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.gamerule.GameRuleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.Permissions;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.cache.PermissionGameRuleInIslandCache;
import fr.euphyllia.skyllia.cache.PermissionRoleInIslandCache;
import fr.euphyllia.skyllia.cache.PlayersInIslandCache;
import fr.euphyllia.skyllia.cache.PositionIslandCache;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

public class ListenersUtils {
    public static @Nullable Island checkGameRuleIsland(Location location, GameRuleIsland gamerule, Cancellable cancellable) {
        Chunk chunk = location.getChunk();
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(chunk.getWorld().getName()))) {
            return null;
        }
        Island island = checkChunkIsIsland(chunk, cancellable);
        if (island == null) {
            return null;
        }
        Position islandOriginPosition = island.getPosition();
        if (checkBlockInIsland(islandOriginPosition, location, island.getSize(), cancellable)) {
            return island;
        }
        long permissionChecker = PermissionGameRuleInIslandCache.getGameruleInIsland(island.getId());

        PermissionManager permissionManager = new PermissionManager(permissionChecker);
        if (permissionManager.hasPermission(gamerule.getPermissionValue())) {
            cancellable.setCancelled(true);
            return island;
        }
        return island;
    }

    public static @Nullable Island checkPermission(Location location, Player player, Permissions permissionsIsland, Cancellable cancellable) {
        Chunk chunk = location.getChunk();
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(chunk.getWorld().getName()))) {
            return null;
        }
        Island island = checkChunkIsIsland(chunk, cancellable);
        if (island == null) {
            return null;
        }
        Position islandOriginPosition = island.getPosition();
        if (checkBlockInIsland(islandOriginPosition, location, island.getSize(), cancellable)) {
            return island;
        }
        Players players = PlayersInIslandCache.getPlayers(island.getId(), player.getUniqueId());
        if (players.getRoleType().equals(RoleType.OWNER)) return island;
        if (players.getRoleType().equals(RoleType.BAN)) {
            cancellable.setCancelled(true);
            return island;
        }
        PermissionRoleIsland permissionRoleIsland;
        if (PlayersInIslandCache.playerIsTrustedInIsland(island.getId(), player.getUniqueId())) {
            permissionRoleIsland = PermissionRoleInIslandCache.getPermissionRoleIsland(island.getId(), RoleType.MEMBER, permissionsIsland.getPermissionType());
        } else {
            permissionRoleIsland = PermissionRoleInIslandCache.getPermissionRoleIsland(island.getId(), players.getRoleType(), permissionsIsland.getPermissionType());
        }
        PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
        if (!permissionManager.hasPermission(permissionsIsland)) {
            cancellable.setCancelled(true);
            return island;
        }
        return island;
    }

    public static Island checkChunkIsIsland(Chunk chunk, Cancellable cancellable) {
        Position position = RegionHelper.getRegionInChunk(chunk.getX(), chunk.getZ());
        Island island = PositionIslandCache.getIsland(position);
        if (island == null) {
            cancellable.setCancelled(true); // Sécurité !
            return null;
        }
        return island;
    }

    public static boolean checkBlockInIsland(Position islandOriginPosition, Location location, double islandSize, Cancellable cancellable) {
        if (!RegionHelper.isBlockWithinRadius(RegionHelper.getCenterRegion(location.getWorld(), islandOriginPosition.x(), islandOriginPosition.z()), location.getBlockX(), location.getBlockZ(), islandSize / 2)) {
            cancellable.setCancelled(true); // ce n'est pas une ile.
            return true;
        }
        return false;
    }


    public static void callPlayerPrepareChangeWorldSkyblockEvent(Player player, PlayerPrepareChangeWorldSkyblockEvent.PortalType portalType, String worldName) {
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(worldName))) {
            return;
        }
        WorldConfig worldConfig = WorldUtils.getWorldConfig(worldName);
        if (worldConfig == null) return;
        Bukkit.getPluginManager().callEvent(new PlayerPrepareChangeWorldSkyblockEvent(player, worldConfig, portalType));
    }
}
