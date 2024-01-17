package fr.euphyllia.skyfolia.listeners.bukkitevents;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.Players;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyfolia.cache.PermissionRoleInIslandCache;
import fr.euphyllia.skyfolia.cache.PlayersInIslandCache;
import fr.euphyllia.skyfolia.cache.PositionIslandCache;
import fr.euphyllia.skyfolia.managers.skyblock.PermissionManager;
import fr.euphyllia.skyfolia.utils.RegionUtils;
import fr.euphyllia.skyfolia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(BlockEvent.class);

    public BlockEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakOnIsland(final BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(block.getWorld().getName()))) {
            return;
        }
        Chunk chunkBlock = event.getBlock().getChunk();
        Island island = PositionIslandCache.getIsland(RegionUtils.getRegionInChunk(chunkBlock.getX(), chunkBlock.getZ()));
        if (island == null) {
            event.setCancelled(true);
            return;
        }
        Players players = PlayersInIslandCache.getPlayers(island.getId(), event.getPlayer().getUniqueId());
        //if (players.getRoleType().equals(RoleType.OWNER)) return; Todo ? à dé-commenter à la fin !
        if (players.getRoleType().equals(RoleType.BAN)) {
            event.setCancelled(true);
            return;
        }
        PermissionRoleIsland permissionRoleIsland = PermissionRoleInIslandCache.getPermissionRoleIsland(island.getId(), players.getRoleType(), PermissionsType.ISLAND);
        PermissionManager permissionManager = new PermissionManager(permissionRoleIsland.permission());
        if (!permissionManager.hasPermission(PermissionsIsland.BLOCK_BREAK)) {
            event.setCancelled(true);
        }

    }

}
