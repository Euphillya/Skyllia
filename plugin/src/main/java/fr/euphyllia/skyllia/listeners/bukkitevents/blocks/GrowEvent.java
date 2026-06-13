package fr.euphyllia.skyllia.listeners.bukkitevents.blocks;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.listeners.ListenersUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

public class GrowEvent implements Listener {

    private final InterneAPI api;
    private final Logger logger = LogManager.getLogger(GrowEvent.class);

    public GrowEvent(InterneAPI interneAPI) {
        this.api = interneAPI;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onStructureGrow(final StructureGrowEvent event) {
        if (!SkylliaAPI.isWorldSkyblock(event.getWorld())) {
            return;
        }

        Location saplingLocation = event.getLocation();
        int chunkX = saplingLocation.getBlockX() >> 4;
        int chunkZ = saplingLocation.getBlockZ() >> 4;
        Island island = ListenersUtils.checkChunkIsIsland(chunkX, chunkZ, event);
        if (island == null) {
            return;
        }

        Position islandRegion = island.getPosition();

        Location center = RegionHelper.getCenterRegion(event.getWorld(), islandRegion.x(), islandRegion.z());

        event.getBlocks().removeIf(blockState -> {
            Location blockLocation = blockState.getLocation();
            Chunk blockChunk = blockLocation.getChunk();
            Position blockRegion = RegionHelper.getRegionFromChunk(blockChunk.getX(), blockChunk.getZ());

            if (blockRegion.x() != islandRegion.x() || blockRegion.z() != islandRegion.z()) {
                return true;
            }

            return !RegionHelper.isBlockWithinSquare(center, blockLocation.getBlockX(), blockLocation.getBlockZ(), island.getSize());
        });
    }
}
