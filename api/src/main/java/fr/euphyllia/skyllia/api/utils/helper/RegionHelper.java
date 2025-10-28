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

    /**
     * Half the region size in blocks (512 / 2 = 256).
     * <p>Used as an offset to find the center of a region.</p>
     */
    private static final double REGION_HALF_SIZE = 256.0D;

    /**
     * Gets the center {@link Location} of a region in the specified world.
     * <p>A region is 512 blocks wide, so shifting {@code regionX} and {@code regionZ} by 9 bits ({@literal <<} 9)
     * multiplies them by 512. Adding {@code REGION_HALF_SIZE} (256) positions us in the center.</p>
     *
     * @param world   The target {@link World}.
     * @param regionX The region's X coordinate.
     * @param regionZ The region's Z coordinate.
     * @return The center location of the region (with Y=0).
     */
    public static Location getCenterRegion(World world, int regionX, int regionZ) {
        double centerBlockX = (regionX << 9) + REGION_HALF_SIZE; // regionX * 512 + 256
        double centerBlockZ = (regionZ << 9) + REGION_HALF_SIZE; // regionZ * 512 + 256
        return new Location(world, centerBlockX, 0.0D, centerBlockZ);
    }

    /**
     * Gets the center chunk position of a region.
     * <p>This calculates the block center of the region, then converts it to chunk coordinates
     * by shifting right by 4 (i.e., dividing by 16).</p>
     *
     * @param regionX The region's X coordinate.
     * @param regionZ The region's Z coordinate.
     * @return A {@link Position} representing the chunk coordinates of the region center.
     */
    public static Position getCenterChunkOfRegion(int regionX, int regionZ) {
        int centerBlockX = (regionX << 9) + (int) REGION_HALF_SIZE;
        int centerBlockZ = (regionZ << 9) + (int) REGION_HALF_SIZE;
        // Convert block coords to chunk coords (>> 4 means /16)
        return new Position(centerBlockX >> 4, centerBlockZ >> 4);
    }

    /**
     * Gets the region position (regionX, regionZ) from the given chunk coordinates.
     * <p>A single region is 32 chunks wide, so shifting right by 5 (>> 5) effectively
     * does {@code chunkCoord / 32}.</p>
     *
     * @param chunkX The chunk's X coordinate.
     * @param chunkZ The chunk's Z coordinate.
     * @return A {@link Position} (regionX, regionZ).
     */
    public static Position getRegionFromChunk(int chunkX, int chunkZ) {
        int regionX = chunkX >> 5; // chunkX / 32
        int regionZ = chunkZ >> 5; // chunkZ / 32
        return new Position(regionX, regionZ);
    }

    /**
     * Gets the region position (regionX, regionZ) from a {@link Location}.
     * <p>This method converts the location to chunk coordinates, then determines
     * the region by dividing by 32 (using bit shifting: {@code >> 5}).</p>
     *
     * @param location The Bukkit {@link Location} (block coordinates).
     * @return A {@link Position} representing the region (regionX, regionZ).
     */
    public static Position getRegionFromLocation(Location location) {
        int chunkX = location.getBlockX() >> 4;
        int chunkZ = location.getBlockZ() >> 4;
        return getRegionFromChunk(chunkX, chunkZ);
    }

    /**
     * Overload for {@link #getRegionFromChunk(int, int)} using a {@link Position} for chunk coordinates.
     *
     * @param chunk A {@link Position} representing chunk coordinates.
     * @return The corresponding region {@link Position}.
     */
    public static Position getRegionFromChunk(Position chunk) {
        return getRegionFromChunk(chunk.x(), chunk.z());
    }

    /**
     * Gets the region position from absolute block coordinates.
     * <p>This first converts blocks to chunk coordinates (>> 4), then chunk to region (>> 5).
     * This is effectively {@code (locX >> 4) >> 5}, i.e. {@code locX >> 9}.</p>
     *
     * @param blockX The absolute block X coordinate.
     * @param blockZ The absolute block Z coordinate.
     * @return The corresponding region {@link Position}.
     */
    public static Position getRegionFromBlock(int blockX, int blockZ) {
        // Convert block -> chunk -> region
        return getRegionFromChunk(blockX >> 4, blockZ >> 4);
    }

    /**
     * Checks if a block is within a square region (bounding box) centered on a given location.
     *
     * <p>Note that this method checks coordinates within a "square" region (bounding box)</p>
     *
     * @param center The center {@link Location}.
     * @param blockX The block's X coordinate.
     * @param blockZ The block's Z coordinate.
     * @param size   The size of the square region (in blocks).
     * @return {@code true} if the block is within the square bounds; {@code false} otherwise.
     */
    public static boolean isBlockWithinSquare(Location center, int blockX, int blockZ, double size) {
        double half = size / 2.0;
        double centerX = center.getX();
        double centerZ = center.getZ();
        double blockXCenter = blockX + 0.5;
        double blockZCenter = blockZ + 0.5;

        return blockXCenter >= centerX - half && blockXCenter < centerX + half &&
                blockZCenter >= centerZ - half && blockZCenter < centerZ + half;
    }

    /**
     * Gets a list of all region positions within a given block range from the specified region.
     * <p>This uses a bounding-box approach, not a strict circle. It shifts the region center back
     * to block coordinates, adjusts by {@link #REGION_HALF_SIZE}, and divides by 512 ({@literal <<} 9) to find
     * how many regions fit in that range.</p>
     *
     * @param regionX    The region's X coordinate.
     * @param regionZ    The region's Z coordinate.
     * @param blockRange The range in blocks around the region center.
     * @return A list of {@link Position} objects representing all regions in that bounding range.
     */
    public static List<Position> getRegionsWithinBlockRange(int regionX, int regionZ, int blockRange) {
        // Convert (blockRange + regionHalfSize) to a region-based radius
        int regionRadius = (blockRange + (int) REGION_HALF_SIZE) >> 9;
        List<Position> regions = new ArrayList<>((2 * regionRadius + 1) * (2 * regionRadius + 1));
        for (int x = -regionRadius; x <= regionRadius; x++) {
            for (int z = -regionRadius; z <= regionRadius; z++) {
                regions.add(new Position(regionX + x, regionZ + z));
            }
        }
        return regions;
    }

    /**
     * Overload of {@link #getRegionsWithinBlockRange(int, int, int)} that takes a {@link Position} for the region.
     *
     * @param position   A {@link Position} representing the region coordinates.
     * @param blockRange The range in blocks around the region center.
     * @return A list of {@link Position} objects representing all regions in that bounding range.
     */
    public static List<Position> getRegionsWithinBlockRange(Position position, int blockRange) {
        return getRegionsWithinBlockRange(position.x(), position.z(), blockRange);
    }

    /**
     * Calculates the total number of chunks in a square region of side length {@code 2 * blockRadius}.
     * <p>It converts the block-based radius to chunks by shifting right by 4 (i.e., dividing by 16),
     * then squares that value.</p>
     *
     * @param blockRadius The radius in blocks.
     * @return The total number of chunks in the resulting (2 * radius) x (2 * radius) bounding box.
     */
    public static int getTotalChunksInBlockRadius(int blockRadius) {
        // (blockRadius * 2) >> 4 => (blockRadius * 2) / 16 => blockRadius / 8
        int chunksPerSide = (blockRadius << 1) >> 4;
        return chunksPerSide * chunksPerSide;
    }

    /**
     * Calculates the total number of chunks in a square region of side length {@code perimeter}.
     * <p>It converts the perimeter in blocks to chunks by shifting right by 4 (dividing by 16),
     * then squares that value.</p>
     *
     * @param blockPerimeter The perimeter (in blocks).
     * @return The total number of chunks in the (perimeter x perimeter) bounding box.
     */
    public static int getTotalChunksInBlockPerimeter(int blockPerimeter) {
        int chunksPerSide = blockPerimeter >> 4; // blockPerimeter / 16
        return chunksPerSide * chunksPerSide;
    }
}
