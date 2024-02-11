package fr.euphyllia.skyllia.listeners.bukkitevents.blocks;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(BlockEvent.class);

    public BlockEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreakOnIsland(final BlockBreakEvent event) {
        if (event.isCancelled()) return;
        ListenersUtils.checkPermission(event.getBlock().getLocation(), event.getPlayer(), PermissionsIsland.BLOCK_BREAK, event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlaceOnIsland(final BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        ListenersUtils.checkPermission(event.getBlock().getLocation(), event.getPlayer(), PermissionsIsland.BLOCK_PLACE, event);
    }
}
