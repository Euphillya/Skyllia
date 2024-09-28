package fr.euphyllia.skyllia.utils;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.utils.models.CallBackPosition;
import fr.euphyllia.skyllia.utils.models.CallbackEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class RegionUtils {

    private static final Logger logger = LogManager.getLogger(RegionUtils.class);
    private static final double OFFSET = 256D;

    public static Position getPositionNewIsland(int start) {
        double r = Math.floor((Math.sqrt(start + 1d) - 1) / 2) + 1;
        double p = (8 * r * (r - 1)) / 2;
        double en = r * 2;
        double a = (start - p) % (r * 8);
        int loc = (int) Math.floor(a / (r * 2));
        int regionX = 0;
        int regionZ = switch (loc) {
            case 0 -> {
                regionX = (int) (a - r);
                yield (int) (-r);
            }
            case 1 -> {
                regionX = (int) r;
                yield (int) ((a % en) - r);
            }
            case 2 -> {
                regionX = (int) (r - (a % en));
                yield (int) r;
            }
            case 3 -> {
                regionX = (int) (-r);
                yield (int) (r - (a % en));
            }
            default -> throw new RuntimeException("A problem with the generation of the island position has occurred.");
        };
        return new Position(regionX, regionZ);
    }

    public static void getEntitiesInRegion(Main plugin, EntityType entityType, World world, Position islandRegion, double size, CallbackEntity callbackEntity) {
        List<Chunk> loadedChunks = new ArrayList<>();

        spiralStartCenter(islandRegion, size, chunkPos -> {
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

        for (int i = 0; i < loadedChunks.size(); i += batchSize) {
            int end = Math.min(i + batchSize, loadedChunks.size());
            List<Chunk> batch = loadedChunks.subList(i, end);

            long delay = (i / batchSize) * delayIncrement;

            if (batch.isEmpty()) continue;

            Chunk representativeChunk = batch.getFirst();
            int repChunkX = representativeChunk.getX();
            int repChunkZ = representativeChunk.getZ();

            Bukkit.getRegionScheduler().runDelayed(plugin, world, repChunkX, repChunkZ, (scheduledTask) -> {
                try {
                    for (Chunk chunk : batch) {
                        for (Entity entity : chunk.getEntities()) {
                            if (entityType == null || entityType == entity.getType()) {
                                callbackEntity.run(entity);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Erreur lors du traitement des chunks", e);
                }
            }, delay);
        }
    }

    /**
     * Méthode pour parcourir les chunks en spirale autour du centre de la région.
     *
     * @param islandRegion        Position centrale de l'île
     * @param size                Taille de l'île (rayon)
     * @param callbackChunkPosition Callback pour traiter chaque chunk position
     */
    public static void spiralStartCenter(Position islandRegion, double size, CallBackPosition callbackChunkPosition) {
        Position chunk = RegionHelper.getChunkCenterRegion(islandRegion.x(), islandRegion.z());
        int cx = chunk.x();
        int cz = chunk.z();
        int x = 0, z = 0;
        int dx = 0, dz = -1;
        int maxI = (int) Math.pow((33 * ConfigToml.regionDistance), 2);
        List<Position> islandPositionWithRadius = RegionHelper.getRegionsInRadius(islandRegion, (int) Math.round(size));
        List<Position> regionCleaned = new ArrayList<>();

        for (int i = 0; i < maxI; i++) {
            if ((-size / 2 <= x) && (x <= size / 2) && (-size / 2 <= z) && (z <= size / 2)) {
                Position chunkPos = new Position(cx + x, cz + z);
                Position region = RegionHelper.getRegionInChunk(chunkPos.x(), chunkPos.z());
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
