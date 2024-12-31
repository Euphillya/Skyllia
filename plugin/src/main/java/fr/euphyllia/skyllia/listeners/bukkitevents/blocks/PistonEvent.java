package fr.euphyllia.skyllia.listeners.bukkitevents.blocks;

import com.google.common.collect.ImmutableMap;
import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import fr.euphyllia.skyllia.utils.WorldUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.Map;

public class PistonEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(PistonEvent.class);
    private final Map<BlockFace, int[]> offsets = ImmutableMap.<BlockFace, int[]>builder()
            .put(BlockFace.EAST, new int[]{1, 0, 0})
            .put(BlockFace.WEST, new int[]{-1, 0, 0})
            .put(BlockFace.UP, new int[]{0, 1, 0})
            .put(BlockFace.DOWN, new int[]{0, -1, 0})
            .put(BlockFace.SOUTH, new int[]{0, 0, 1})
            .put(BlockFace.NORTH, new int[]{0, 0, -1})
            .build();

    public PistonEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (event.isCancelled()) return;
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(event.getBlock().getWorld().getName()))) {
            return;
        }
        int[] offset = offsets.get(event.getDirection());
        for (Block block : event.getBlocks()) {
            Location location = block.getLocation().add(offset[0], offset[1], offset[2]);
            Chunk chunk = location.getChunk();
            Island island = ListenersUtils.checkChunkIsIsland(chunk, event);
            if (island == null) {
                return;
            }
            if (ListenersUtils.isBlockOutsideIsland(island, location, event)) {
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isCancelled()) return;
        if (Boolean.FALSE.equals(WorldUtils.isWorldSkyblock(event.getBlock().getWorld().getName()))) {
            return;
        }
        for (Block block : event.getBlocks()) {
            Location location = block.getLocation();
            Chunk chunk = location.getChunk();
            Island island = ListenersUtils.checkChunkIsIsland(chunk, event);
            if (island == null) {
                return;
            }
            if (ListenersUtils.isBlockOutsideIsland(island, location, event)) {
                return;
            }
        }
    }
}
