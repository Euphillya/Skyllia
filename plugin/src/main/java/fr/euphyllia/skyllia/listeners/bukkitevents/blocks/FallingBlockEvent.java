package fr.euphyllia.skyllia.listeners.bukkitevents.blocks;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class FallingBlockEvent implements Listener {

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFallingBlockLand(final EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;

        World world = event.getBlock().getWorld();
        if (!SkylliaAPI.isWorldSkyblock(world.getName())) return;

        Location loc = event.getBlock().getLocation();
        int bx = loc.getBlockX();
        int bz = loc.getBlockZ();

        Island island = SkylliaAPI.getIslandByChunk(bx >> 4, bz >> 4);
        if (island == null) {
            event.setCancelled(true);
            event.getEntity().remove();
            return;
        }

        ListenersUtils.isBlockOutsideIsland(island, world, bx, loc.getBlockY(), bz, event);
        if (event.isCancelled()) {
            event.getEntity().remove();
        }
    }
}
