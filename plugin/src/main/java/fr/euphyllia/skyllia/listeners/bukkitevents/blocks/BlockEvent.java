package fr.euphyllia.skyllia.listeners.bukkitevents.blocks;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.PermissionImp;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

public class BlockEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(BlockEvent.class);

    public BlockEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakOnIsland(final BlockBreakEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (PermissionImp.hasPermission(player, "skyllia.player.break.bypass")) return;
        ListenersUtils.checkPermission(event.getBlock().getLocation(), player, PermissionsIsland.BLOCK_BREAK, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlaceOnIsland(final BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        if (PermissionImp.hasPermission(player, "skyllia.player.place.bypass")) return;
        ListenersUtils.checkPermission(event.getBlock().getLocation(), player, PermissionsIsland.BLOCK_PLACE, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreakByEntityEvent(final HangingBreakByEntityEvent event) {
        if (event.isCancelled()) return;
        if (event.getEntity() instanceof ItemFrame || event.getEntity() instanceof Painting) {
            Player player;
            if (event.getRemover() instanceof Projectile projectile && projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            } else if (event.getRemover() instanceof Player) {
                player = (Player) event.getRemover();
            } else {
                return;
            }

            if (PermissionImp.hasPermission(player, "skyllia.player.break.bypass")) {
                return;
            }
            ListenersUtils.checkPermission(event.getEntity().getLocation(), player, PermissionsIsland.BLOCK_BREAK, event);
        }
    }
}
