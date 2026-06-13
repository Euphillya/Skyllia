package fr.euphyllia.skyllia.listeners.bukkitevents.blocks;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

public class PistonEvent implements Listener {

    private static final int[][] OFFSETS = buildOffsets();
    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(PistonEvent.class);

    public PistonEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    private static int[][] buildOffsets() {
        int[][] arr = new int[BlockFace.values().length][];
        for (BlockFace face : new BlockFace[]{
                BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH,
                BlockFace.WEST, BlockFace.UP, BlockFace.DOWN}) {
            arr[face.ordinal()] = new int[]{face.getModX(), face.getModY(), face.getModZ()};
        }
        return arr;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        World world = event.getBlock().getWorld();
        if (!SkylliaAPI.isWorldSkyblock(world.getName())) {
            return;
        }
        int[] offset = OFFSETS[event.getDirection().ordinal()];
        for (Block block : event.getBlocks()) {
            Location location = block.getLocation().add(offset[0], offset[1], offset[2]);
            int blockX = location.getBlockX();
            int blockZ = location.getBlockZ();
            int chunkX = blockX >> 4;
            int chunkZ = blockZ >> 4;
            Island island = ListenersUtils.checkChunkIsIsland(chunkX, chunkZ, event);
            if (island == null) {
                return;
            }

            ListenersUtils.isBlockOutsideIsland(island, world, blockX, location.getBlockY(), blockZ, event);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        World world = event.getBlock().getWorld();
        if (!SkylliaAPI.isWorldSkyblock(world.getName())) {
            return;
        }
        for (Block block : event.getBlocks()) {
            Location location = block.getLocation();
            int blockX = location.getBlockX();
            int blockZ = location.getBlockZ();
            int chunkX = blockX >> 4;
            int chunkZ = blockZ >> 4;
            Island island = ListenersUtils.checkChunkIsIsland(chunkX, chunkZ, event);
            if (island == null) {
                return;
            }
            ListenersUtils.isBlockOutsideIsland(island, world, blockX, location.getBlockY(), blockZ, event);
        }
    }
}
