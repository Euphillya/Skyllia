package fr.euphyllia.skyllia.api.utils;

import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.api.utils.models.CallBackPosition;
import fr.euphyllia.skyllia.api.utils.models.CallbackEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RegionUtils {

    private static final Logger logger = LogManager.getLogger(RegionUtils.class);
    private static final double OFFSET = 256D;


    /**
     * Computes a new island position (regionX, regionZ) based on a specific sequential index.
     *
     * <p>The algorithm generates a spiral-like sequence of region coordinates around the origin,
     * ensuring islands are placed in non-overlapping positions.</p>
     *
     * @param index Sequential index of the new island (e.g., island count).
     * @return A {@link Position} representing the region coordinates (regionX, regionZ) for the new island.
     */
    public static Position computeNewIslandRegionPosition(int index) {
        // ring = floor((sqrt(index + 1) - 1) / 2) + 1
        double ring = Math.floor((Math.sqrt(index + 1d) - 1) / 2) + 1;

        // partial = (8 * ring * (ring - 1)) / 2
        double partial = (8 * ring * (ring - 1)) / 2;

        // ringDouble = ring * 2
        double ringDouble = ring * 2;

        // a = (index - partial) % (ring * 8)
        double a = (index - partial) % (ring * 8);

        // locationOnEdge = floor(a / (ring * 2))
        int locationOnEdge = (int) Math.floor(a / (ring * 2));

        int regionX;
        int regionZ;

        // Depending on which edge of the ring we are on, compute regionX / regionZ
        switch (locationOnEdge) {
            case 0 -> {
                regionX = (int) (a - ring);
                regionZ = (int) -ring;
            }
            case 1 -> {
                regionX = (int) ring;
                regionZ = (int) ((a % ringDouble) - ring);
            }
            case 2 -> {
                regionX = (int) (ring - (a % ringDouble));
                regionZ = (int) ring;
            }
            case 3 -> {
                regionX = (int) -ring;
                regionZ = (int) (ring - (a % ringDouble));
            }
            default -> throw new RuntimeException("Island region generation encountered an unexpected index.");
        }
        return new Position(regionX, regionZ);
    }

    /**
     * Retrieves all entities of the specified {@link EntityType} (or all entities if {@code entityType} is null)
     * within a given island region and a specified bounding size. The chunks are collected via a spiral traversal
     * and processed in batches to avoid blocking the main thread.
     *
     * @param plugin         The {@link JavaPlugin} plugin instance.
     * @param regionDistance The distance between the islands
     * @param entityType     The {@link EntityType} to filter, or {@code null} for all types.
     * @param world          The {@link World} where we search for entities.
     * @param islandRegion   The central region of the island.
     * @param islandSize     The size (in blocks) of the island bounding box.
     * @param callbackEntity A callback that will be invoked for each matching {@link Entity}.
     */
    public static void getEntitiesInRegion(JavaPlugin plugin,
                                           int regionDistance,
                                           EntityType entityType,
                                           World world,
                                           Position islandRegion,
                                           double islandSize,
                                           CallbackEntity callbackEntity) {

        // 1. Collect loaded chunks via spiral traversal
        List<Chunk> loadedChunks = new ArrayList<>();
        spiralTraverseAroundRegion(regionDistance, islandRegion, islandSize, chunkPos -> {
            int chunkX = chunkPos.x();
            int chunkZ = chunkPos.z();

            // Check if the chunk is loaded, then add it to the list
            if (world.isChunkLoaded(chunkX, chunkZ)) {
                Chunk chunk = world.getChunkAt(chunkX, chunkZ, false);
                if (chunk.isLoaded()) {
                    loadedChunks.add(chunk);
                }
            }
        });

        // 2. Process chunks in batches to avoid large synchronous workloads
        int batchSize = 16;  // Number of chunks to process per batch
        int delayIncrement = 1;

        for (int startIndex = 0; startIndex < loadedChunks.size(); startIndex += batchSize) {
            int endIndex = Math.min(startIndex + batchSize, loadedChunks.size());
            List<Chunk> batch = loadedChunks.subList(startIndex, endIndex);

            if (batch.isEmpty()) {
                continue;
            }

            // We'll create a small delay between each batch
            long delay = (long) ((startIndex / (double) batchSize) * delayIncrement);

            // Use the first chunk in the batch as a "representative" for region scheduling
            Chunk representativeChunk = batch.get(0);
            int repChunkX = representativeChunk.getX();
            int repChunkZ = representativeChunk.getZ();

            // Schedule processing of this batch on the region scheduler
            Bukkit.getRegionScheduler().runDelayed(plugin, world, repChunkX, repChunkZ, (scheduledTask) -> {
                try {
                    for (Chunk chunk : batch) {
                        for (Entity entity : chunk.getEntities()) {
                            // If entityType is null, accept all; otherwise match entity type
                            if (entityType == null || entity.getType() == entityType) {
                                callbackEntity.run(entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error while processing chunks in batch", e);
                }
            }, Math.max(1, delay));
        }
    }


    /**
     * Traverses chunks in a spiral pattern around the center of a region, up to a specified bounding size.
     *
     * <p>The spiral ensures we explore chunks outward from the region's center, which is calculated from
     * {@link RegionHelper#getCenterChunkOfRegion(int, int)}. The chunk positions are then checked against
     * the island's region boundary.</p>
     *
     * @param regionDistance        The distance between the islands.
     * @param islandRegion          The central island region (regionX, regionZ).
     * @param size                  The bounding size (in blocks) to define the spiral limit.
     * @param callbackChunkPosition A callback invoked with each chunk position in the spiral traversal.
     */
    public static void spiralTraverseAroundRegion(int regionDistance, Position islandRegion, double size, CallBackPosition callbackChunkPosition) {
        // Determine the chunk coordinates at the center of the region
        Position centerChunkPos = RegionHelper.getCenterChunkOfRegion(islandRegion.x(), islandRegion.z());
        int centerChunkX = centerChunkPos.x();
        int centerChunkZ = centerChunkPos.z();

        // Spiral state
        int x = 0, z = 0;
        int dx = 0, dz = -1;

        // We define a maximum to avoid infinite loops or extremely large searches
        int maxIterations = (int) Math.pow(33 * regionDistance, 2);

        // Retrieve all regions within the specified block size around the island region
        List<Position> validRegions = RegionHelper.getRegionsWithinBlockRange(islandRegion, (int) Math.round(size));

        // We'll track visited regions to avoid repeated checks
        Set<Position> visitedRegions = new HashSet<>();

        // Spiral iteration
        for (int i = 0; i < maxIterations; i++) {
            // Check if (x, z) is within half-size in both directions (square check)
            if ((-size / 2 <= x) && (x <= size / 2) && (-size / 2 <= z) && (z <= size / 2)) {
                // Actual chunk coordinates
                Position chunkPos = new Position(centerChunkX + x, centerChunkZ + z);

                // Convert the chunk coordinates to region coordinates
                Position region = RegionHelper.getRegionFromChunk(chunkPos.x(), chunkPos.z());

                // If the region is inside our valid region boundary
                if (validRegions.contains(region)) {
                    // Check if we have processed it yet
                    if (!visitedRegions.contains(chunkPos)) {
                        // Invoke callback on the chunk position
                        callbackChunkPosition.run(chunkPos);
                    }
                    visitedRegions.add(chunkPos);
                }
            }

            // Spiral direction change logic
            // If we are on a diagonal or a turning point, rotate the direction
            if ((x == z) || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }
            x += dx;
            z += dz;
        }
    }

    /**
     * Méthode pour parcourir les chunks en spirale autour du centre de la région.
     *
     * @param islandRegion          Position centrale de l'île
     * @param regionDistance        The distance between the islands
     * @param size                  Taille de l'île (rayon)
     * @param callbackChunkPosition Callback pour traiter chaque chunk position
     */
    public static void spiralStartCenter(Position islandRegion, int regionDistance, double size, CallBackPosition callbackChunkPosition) {
        Position chunk = RegionHelper.getCenterChunkOfRegion(islandRegion.x(), islandRegion.z());
        int cx = chunk.x();
        int cz = chunk.z();
        int x = 0, z = 0;
        int dx = 0, dz = -1;
        int maxI = (int) Math.pow((33 * regionDistance), 2);
        List<Position> islandPositionWithRadius = RegionHelper.getRegionsWithinBlockRange(islandRegion, (int) Math.round(size));
        List<Position> regionCleaned = new ArrayList<>();

        for (int i = 0; i < maxI; i++) {
            if ((-size / 2 <= x) && (x <= size / 2) && (-size / 2 <= z) && (z <= size / 2)) {
                Position chunkPos = new Position(cx + x, cz + z);
                Position region = RegionHelper.getRegionFromChunk(chunkPos.x(), chunkPos.z());
                if (islandPositionWithRadius.contains(region)) {
                    if (!regionCleaned.contains(region)) {
                        regionCleaned.add(region);
                    }
                    callbackChunkPosition.run(chunkPos);
                }
            }

            if ((x == z) || ((x < 0) && (x == -z)) || ((x > 0) && (x == 1 - z))) {
                int temp = dx;
                dx = -dz;
                dz = temp;
            }
            x += dx;
            z += dz;
        }
    }
}
