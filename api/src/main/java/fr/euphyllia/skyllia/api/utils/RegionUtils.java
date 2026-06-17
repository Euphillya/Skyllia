package fr.euphyllia.skyllia.api.utils;

import fr.euphyllia.skyllia.api.coordinate.ChunkCoordinate;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
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
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

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
     * @return A {@link RegionCoordinate} representing the region coordinates (regionX, regionZ) for the new island.
     */
    public static RegionCoordinate computeNewIslandRegionPosition(int index) {
        double ring = Math.floor((Math.sqrt(index + 1d) - 1) / 2) + 1;
        double partial = (8 * ring * (ring - 1)) / 2;
        double ringDouble = ring * 2;
        double a = (index - partial) % (ring * 8);
        int locationOnEdge = (int) Math.floor(a / (ring * 2));

        int regionX;
        int regionZ;

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
        return new RegionCoordinate(regionX, regionZ);
    }

    /**
     * Retrieves all entities of the specified {@link EntityType} (or all entities if {@code entityType} is null)
     * within a given island region and a specified bounding size.
     *
     * @param plugin         The {@link JavaPlugin} plugin instance.
     * @param regionDistance The distance between the islands.
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
                                           RegionCoordinate islandRegion,
                                           double islandSize,
                                           CallbackEntity callbackEntity) {

        List<Chunk> loadedChunks = new ArrayList<>();
        spiralTraverseAroundRegion(regionDistance, islandRegion, islandSize, chunkPos -> {
            int chunkX = chunkPos.x();
            int chunkZ = chunkPos.z();
            if (world.isChunkLoaded(chunkX, chunkZ)) {
                Chunk chunk = world.getChunkAt(chunkX, chunkZ, false);
                if (chunk.isLoaded()) {
                    loadedChunks.add(chunk);
                }
            }
        });

        int batchSize = 16;
        int delayIncrement = 1;

        for (int startIndex = 0; startIndex < loadedChunks.size(); startIndex += batchSize) {
            int endIndex = Math.min(startIndex + batchSize, loadedChunks.size());
            List<Chunk> batch = loadedChunks.subList(startIndex, endIndex);
            if (batch.isEmpty()) continue;

            long delay = (long) ((startIndex / (double) batchSize) * delayIncrement);
            Chunk representativeChunk = batch.getFirst();
            int repChunkX = representativeChunk.getX();
            int repChunkZ = representativeChunk.getZ();

            Bukkit.getRegionScheduler().runDelayed(plugin, world, repChunkX, repChunkZ, (scheduledTask) -> {
                try {
                    for (Chunk chunk : batch) {
                        for (Entity entity : chunk.getEntities()) {
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
     * Retrieves all entities within a given island region.
     *
     * @param plugin         The {@link JavaPlugin} plugin instance.
     * @param regionDistance The distance between the islands.
     * @param entityType     The {@link EntityType} to filter, or {@code null} for all types.
     * @param world          The {@link World} where we search for entities.
     * @param islandRegion   The central region of the island as a {@link Position}.
     * @param islandSize     The size (in blocks) of the island bounding box.
     * @param callbackEntity A callback that will be invoked for each matching {@link Entity}.
     * @deprecated Use {@link #getEntitiesInRegion(JavaPlugin, int, EntityType, World, RegionCoordinate, double, CallbackEntity)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public static void getEntitiesInRegion(JavaPlugin plugin,
                                           int regionDistance,
                                           EntityType entityType,
                                           World world,
                                           Position islandRegion,
                                           double islandSize,
                                           CallbackEntity callbackEntity) {
        getEntitiesInRegion(plugin, regionDistance, entityType, world,
                new RegionCoordinate(islandRegion.x(), islandRegion.z()), islandSize, callbackEntity);
    }

    /**
     * Traverses chunks in a spiral pattern around the center of a region.
     *
     * @param regionDistance        The distance between the islands.
     * @param islandRegion          The central island region.
     * @param size                  The bounding size (in blocks) to define the spiral limit.
     * @param callbackChunkPosition A callback invoked with each {@link ChunkCoordinate} in the spiral traversal.
     */
    public static void spiralTraverseAroundRegion(int regionDistance, RegionCoordinate islandRegion, double size,
                                                  Consumer<ChunkCoordinate> callbackChunkPosition) {
        ChunkCoordinate centerChunkPos = RegionHelper.getCenterChunkOfChunk(islandRegion.x(), islandRegion.z());
        int centerChunkX = centerChunkPos.x();
        int centerChunkZ = centerChunkPos.z();

        int x = 0, z = 0;
        int dx = 0, dz = -1;
        int maxIterations = (int) Math.pow(33 * regionDistance, 2);

        Set<RegionCoordinate> validRegions = new HashSet<>(RegionHelper.getRegionsWithinBlockRange(islandRegion, (int) Math.round(size)));
        Set<ChunkCoordinate> visitedChunks = new HashSet<>();

        for (int i = 0; i < maxIterations; i++) {
            if ((-size / 2 <= x) && (x <= size / 2) && (-size / 2 <= z) && (z <= size / 2)) {
                ChunkCoordinate chunkPos = new ChunkCoordinate(centerChunkX + x, centerChunkZ + z);
                RegionCoordinate region = RegionHelper.getRegionCoordinateFromChunk(chunkPos.x(), chunkPos.z());
                if (validRegions.contains(region)) {
                    if (!visitedChunks.contains(chunkPos)) {
                        callbackChunkPosition.accept(chunkPos);
                    }
                    visitedChunks.add(chunkPos);
                }
            }

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
     * Traverses chunks in a spiral pattern around the center of a region.
     *
     * @param regionDistance        The distance between the islands.
     * @param islandRegion          The central island region as a {@link Position}.
     * @param size                  The bounding size (in blocks) to define the spiral limit.
     * @param callbackChunkPosition A callback invoked with each chunk position.
     * @deprecated Use {@link #spiralTraverseAroundRegion(int, RegionCoordinate, double, Consumer)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public static void spiralTraverseAroundRegion(int regionDistance, Position islandRegion, double size,
                                                  CallBackPosition callbackChunkPosition) {
        spiralTraverseAroundRegion(regionDistance, new RegionCoordinate(islandRegion.x(), islandRegion.z()), size,
                chunk -> callbackChunkPosition.run(new Position(chunk.x(), chunk.z())));
    }

    /**
     * Traverses chunks in a spiral pattern starting from the center of a region.
     *
     * @param islandRegion          The central region of the island.
     * @param regionDistance        The distance between the islands.
     * @param size                  The size of the island (radius).
     * @param callbackChunkPosition Callback to process each {@link ChunkCoordinate}.
     */
    public static void spiralStartCenter(RegionCoordinate islandRegion, int regionDistance, double size,
                                         Consumer<ChunkCoordinate> callbackChunkPosition) {
        ChunkCoordinate chunk = RegionHelper.getCenterChunkOfChunk(islandRegion.x(), islandRegion.z());
        int cx = chunk.x();
        int cz = chunk.z();
        int x = 0, z = 0;
        int dx = 0, dz = -1;
        int maxI = (int) Math.pow((33 * regionDistance), 2);
        List<RegionCoordinate> islandPositionWithRadius = RegionHelper.getRegionsWithinBlockRange(islandRegion, (int) Math.round(size));
        List<RegionCoordinate> regionCleaned = new ArrayList<>();

        for (int i = 0; i < maxI; i++) {
            if ((-size / 2 <= x) && (x <= size / 2) && (-size / 2 <= z) && (z <= size / 2)) {
                ChunkCoordinate chunkPos = new ChunkCoordinate(cx + x, cz + z);
                RegionCoordinate region = RegionHelper.getRegionCoordinateFromChunk(chunkPos.x(), chunkPos.z());
                if (islandPositionWithRadius.contains(region)) {
                    if (!regionCleaned.contains(region)) {
                        regionCleaned.add(region);
                    }
                    callbackChunkPosition.accept(chunkPos);
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

    /**
     * Traverses chunks in a spiral pattern starting from the center of a region.
     *
     * @param islandRegion          Position centrale de l'île as a {@link Position}.
     * @param regionDistance        The distance between the islands.
     * @param size                  The size of the island (radius).
     * @param callbackChunkPosition Callback to process each chunk position.
     * @deprecated Use {@link #spiralStartCenter(RegionCoordinate, int, double, Consumer)} instead.
     */
    @Deprecated(forRemoval = true, since = "3.x")
    @ApiStatus.ScheduledForRemoval(inVersion = "4.x")
    public static void spiralStartCenter(Position islandRegion, int regionDistance, double size,
                                         CallBackPosition callbackChunkPosition) {
        spiralStartCenter(new RegionCoordinate(islandRegion.x(), islandRegion.z()), regionDistance, size,
                chunk -> callbackChunkPosition.run(new Position(chunk.x(), chunk.z())));
    }

    /**
     * Computes the list of chunk coordinates to delete for the given island region.
     *
     * @param islandRegion   The island's region coordinate.
     * @param regionDistance The distance between islands.
     * @param size           The size of the island.
     * @return A list of {@link ChunkCoordinate} to delete.
     */
    public static List<ChunkCoordinate> computeChunksToDelete(RegionCoordinate islandRegion, int regionDistance, double size) {
        List<ChunkCoordinate> chunkPositions = new ArrayList<>();
        spiralStartCenter(islandRegion, regionDistance, size, chunkPositions::add);
        return chunkPositions;
    }

    /**
     * Computes the list of chunk coordinates to delete for the given island region.
     *
     * @param islandRegion   The island's region coordinate as a {@link Position}.
     * @param regionDistance The distance between islands.
     * @param size           The size of the island.
     * @return A list of {@link ChunkCoordinate} to delete.
     * @deprecated Use {@link #computeChunksToDelete(RegionCoordinate, int, double)} instead.
     */
    @Deprecated(forRemoval = false, since = "3.x")
    public static List<ChunkCoordinate> computeChunksToDelete(Position islandRegion, int regionDistance, double size) {
        return computeChunksToDelete(new RegionCoordinate(islandRegion.x(), islandRegion.z()), regionDistance, size);
    }
}