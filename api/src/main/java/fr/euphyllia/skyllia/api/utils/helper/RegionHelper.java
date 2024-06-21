package fr.euphyllia.skyllia.api.utils.helper;

import fr.euphyllia.skyllia.api.skyblock.model.Position;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides helper methods for working with regions and positions in a Minecraft world.
 */
public class RegionHelper {
    private static final double OFFSET = 256D;

    /**
     * Gets the center location of a region in the specified world.
     *
     * @param w The world.
     * @param regionX The X coordinate of the region.
     * @param regionZ The Z coordinate of the region.
     * @return The center location of the region.
     */
    public static Location getCenterRegion(World w, int regionX, int regionZ) {
        double rx = (regionX << 9) + OFFSET;
        double rz = (regionZ << 9) + OFFSET;
        return new Location(w, rx, 0.0d, rz);
    }

    /**
     * Gets the center chunk position of a region.
     *
     * @param regionX The X coordinate of the region.
     * @param regionZ The Z coordinate of the region.
     * @return The center chunk position of the region.
     */
    public static Position getChunkCenterRegion(int regionX, int regionZ) {
        int chunkX = (regionX << 9) + (int) OFFSET;
        int chunkZ = (regionZ << 9) + (int) OFFSET;

        return new Position(chunkX >> 4, chunkZ >> 4);
    }

    /**
     * Gets the region position from chunk coordinates.
     *
     * @param chunkX The X coordinate of the chunk.
     * @param chunkZ The Z coordinate of the chunk.
     * @return The region position.
     */
    public static Position getRegionInChunk(int chunkX, int chunkZ) {
        int regionX = chunkX >> 5;
        int regionZ = chunkZ >> 5;
        return new Position(regionX, regionZ);
    }

    /**
     * Gets the region position from a chunk position.
     *
     * @param chunk The chunk position.
     * @return The region position.
     */
    public static Position getRegionInChunk(Position chunk) {
        return getRegionInChunk(chunk.x(), chunk.z());
    }

    /**
     * Gets the region position from location coordinates.
     *
     * @param locX The X coordinate of the location.
     * @param locZ The Z coordinate of the location.
     * @return The region position.
     */
    public static Position getRegionWithLocation(int locX, int locZ) {
        return getRegionInChunk(locX >> 4, locZ >> 4);
    }

    /**
     * Checks if a block is within a specified radius of a center location.
     *
     * @param center The center location.
     * @param blockX The X coordinate of the block.
     * @param blockZ The Z coordinate of the block.
     * @param radius The radius to check within.
     * @return True if the block is within the radius, false otherwise.
     */
    public static boolean isBlockWithinRadius(Location center, int blockX, int blockZ, double radius) {
        int centerX = center.getBlockX();
        int centerZ = center.getBlockZ();

        return Math.abs(centerX - blockX) <= radius && Math.abs(centerZ - blockZ) <= radius;
    }

    /**
     * Gets a list of regions within a specified block radius of a position.
     *
     * @param position The center position.
     * @param blockRadius The block radius.
     * @return A list of regions within the radius.
     */
    public static List<Position> getRegionsInRadius(Position position, int blockRadius) {
        return getRegionsInRadius(position.x(), position.z(), blockRadius);
    }

    /**
     * Gets a list of regions within a specified block radius of a region.
     *
     * @param regionX The X coordinate of the region.
     * @param regionZ The Z coordinate of the region.
     * @param blockRadius The block radius.
     * @return A list of regions within the radius.
     */
    public static List<Position> getRegionsInRadius(int regionX, int regionZ, int blockRadius) {
        int centerBlockX = (regionX << 9) + (int) 256D;
        int centerBlockZ = (regionZ << 9) + (int) 256D;

        List<Position> regions = new ArrayList<>();
        int regionRadius = (blockRadius + (int) 256D) >> 9;

        for (int x = -regionRadius; x <= regionRadius; x++) {
            for (int z = -regionRadius; z <= regionRadius; z++) {
                int regionXCoord = (centerBlockX >> 9) + x;
                int regionZCoord = (centerBlockZ >> 9) + z;
                regions.add(new Position(regionXCoord, regionZCoord));
            }
        }

        return regions;
    }

    /**
     * Gets the total number of chunks within a specified radius.
     *
     * @param rayon The radius in blocks.
     * @return The total number of chunks.
     */
    public static int getNumberChunkTotalInRayon(int rayon) {
        int chunksParCote = (rayon << 1) >> 4; // (rayon * 2) / 16
        return chunksParCote * chunksParCote;
    }

    /**
     * Gets the total number of chunks within a specified perimeter.
     *
     * @param perimeter The perimeter in blocks.
     * @return The total number of chunks.
     */
    public static int getNumberChunkTotalInPerimeter(int perimeter) {
        int chunksParCote = perimeter >> 4; // perimeter / 16
        return chunksParCote * chunksParCote;
    }
}
