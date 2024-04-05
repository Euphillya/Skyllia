package fr.euphyllia.skyllia.utils;

import fr.euphyllia.energie.model.MultipleRecords;
import fr.euphyllia.energie.model.SchedulerType;
import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.helper.RegionHelper;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.utils.models.CallBackPosition;
import fr.euphyllia.skyllia.utils.models.CallbackEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Chunk;
import org.bukkit.Location;
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

    public static void getEntitiesInRegion(Main plugin, EntityType entityType, World world, int regionX, int regionZ, CallbackEntity callbackEntity) {
        int minChunkX = regionX << 5; // Calcul de la coordonnée X minimale du chunk
        int minChunkZ = regionZ << 5; // Calcul de la coordonnée Z minimale du chunk

        int maxChunkX = minChunkX + 31; // 32 chunks en X
        int maxChunkZ = minChunkZ + 31; // 32 chunks en Z

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                final int chunkX = x;
                final int chunkZ = z;
                SkylliaAPI.getScheduler()
                        .runDelayed(SchedulerType.SYNC, new MultipleRecords.WorldChunk(world, chunkX, chunkZ), schedulerTask -> {
                            Chunk chunk = world.getChunkAt(chunkX, chunkZ);
                            if (chunk.isLoaded()) {
                                // Traitement du chunk chargé
                                Entity[] listEntities = chunk.getEntities();
                                for (Entity entity : listEntities) {
                                    if (entityType == entity.getType() || entityType == null) {
                                        callbackEntity.run(entity);
                                    }
                                }
                            }
                        }, 1);
            }
        }
    }

    public static void spiralStartCenter(Position islandRegion, double size, CallBackPosition callbackChunkPosition) {
        Position chunk = RegionHelper.getChunkCenterRegion(islandRegion.x(), islandRegion.z());
        int cx = chunk.x();
        int cz = chunk.z();
        int x = 0, z = 0;
        int dx = 0, dz = -1;
        int maxI = (33 * ConfigToml.regionDistance) * (33 * ConfigToml.regionDistance);
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
