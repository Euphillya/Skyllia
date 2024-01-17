package fr.euphyllia.skyfolia.listeners;

import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.Permissions;
import fr.euphyllia.skyfolia.cache.PermissionRoleInIslandCache;
import fr.euphyllia.skyfolia.cache.PlayersInIslandCache;
import fr.euphyllia.skyfolia.cache.PositionIslandCache;
import fr.euphyllia.skyfolia.managers.skyblock.PermissionManager;
import fr.euphyllia.skyfolia.utils.RegionUtils;
import fr.euphyllia.skyfolia.utils.WorldUtils;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

public class ListenersUtils {

    public static void checkPermission(Chunk chunk, Player player, Permissions permissionsIsland, Cancellable cancellable) {
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(chunk.getWorld().getName()))) {
            return;
        }
        Island island = PositionIslandCache.getIsland(RegionUtils.getRegionInChunk(chunk.getX(), chunk.getZ()));
        if (island == null) {
            cancellable.setCancelled(true); // Sécurité !
            return;
        }
        Players players = PlayersInIslandCache.getPlayers(island.getId(), player.getUniqueId());
        //if (players.getRoleType().equals(RoleType.OWNER)) return; Todo ? à dé-commenter à la fin !
        if (players.getRoleType().equals(RoleType.BAN)) {
            cancellable.setCancelled(true);
            return;
        }
        PermissionRoleIsland permissionRoleIsland = PermissionRoleInIslandCache.getPermissionRoleIsland(island.getId(), players.getRoleType(), permissionsIsland.getPermissionType());
        PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
        if (!permissionManager.hasPermission(permissionsIsland)) {
            cancellable.setCancelled(true);
        }
    }
}
