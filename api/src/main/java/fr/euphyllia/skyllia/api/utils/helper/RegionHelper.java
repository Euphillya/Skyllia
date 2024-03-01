package fr.euphyllia.skyllia.api.utils.helper;

import fr.euphyllia.skyllia.api.skyblock.model.Position;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class RegionHelper {
    private static final double OFFSET = 256D;

    public static Location getCenterRegion(World w, int regionX, int regionZ) {
        double rx = (regionX << 9) + OFFSET;
        double rz = (regionZ << 9) + OFFSET;
        return new Location(w, rx, 0.0d, rz);
    }

    public static Position getChunkCenterRegion(int regionX, int regionZ) {
        int chunkX = (regionX << 9) + (int) OFFSET;
        int chunkZ = (regionZ << 9) + (int) OFFSET;

        return new Position(chunkX >> 4, chunkZ >> 4);
    }

    public static Position getRegionInChunk(int chunkX, int chunkZ) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        return new Position(regionX, regionZ);
    }

    public static Position getRegionInChunk(Position chunk) {
        return getRegionInChunk(chunk.x(), chunk.z());
    }

    public static Position getRegionWithLocation(int locX, int locZ) {
        return getRegionInChunk(locX >> 4, locZ >> 4);
    }

    public static boolean isBlockWithinRadius(Location center, int blockX, int blockZ, double radius) {
        double dx = (double) center.getBlockX() - blockX;
        double dz = (double) center.getBlockZ() - blockZ;
        double distance = Math.sqrt(dx * dx + dz * dz);

        return distance <= radius;
    }

    public static List<Position> getRegionsInRadius(Position position, int blockRadius) {
        return getRegionsInRadius(position.x(), position.z(), blockRadius);
    }

    public static List<Position> getRegionsInRadius(int regionX, int regionZ, int blockRadius) {
        int centerBlockX = (regionX << 9) + (int) OFFSET;
        int centerBlockZ = (regionZ << 9) + (int) OFFSET;

        List<Position> regions = new ArrayList<>();
        int regionRadius = (blockRadius + (int) OFFSET) >> 9;

        for (int x = -regionRadius; x <= regionRadius; x++) {
            for (int z = -regionRadius; z <= regionRadius; z++) {
                int regionXCoord = (centerBlockX >> 9) + x;
                int regionZCoord = (centerBlockZ >> 9) + z;
                regions.add(new Position(regionXCoord, regionZCoord));
            }
        }

        return regions;
    }

    public static int getNumberChunkTotalInRayon(int rayon) {
        int chunksParCote = (rayon << 1) >> 4; // (rayon * 2) / 16
        return chunksParCote * chunksParCote;
    }

    public static int getNumberChunkTotalInPerimeter(int perimenter) {
        int chunksParCote = perimenter >> 4; // perimenter / 16
        return chunksParCote * chunksParCote;
    }
}
