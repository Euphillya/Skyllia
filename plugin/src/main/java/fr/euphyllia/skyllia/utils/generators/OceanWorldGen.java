package fr.euphyllia.skyllia.utils.generators;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class OceanWorldGen extends ChunkGenerator {

    private final int seaHeight;
    private final Material seaBlock;

    public OceanWorldGen(int seaHeight, Material seaBlock) {
        this.seaHeight = seaHeight;
        this.seaBlock = (seaBlock != null) ? seaBlock : Material.WATER;
    }

    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        if (seaHeight <= worldInfo.getMinHeight()) return;

        int minY = worldInfo.getMinHeight();
        int maxY = worldInfo.getMaxHeight();

        if (seaHeight <= minY) return;

        int clampedSea = Math.min(seaHeight, maxY - 1);

        if (clampedSea >= minY + 1) {
            chunkData.setRegion(0, minY + 1, 0, 16, clampedSea + 1, 16, seaBlock);
        }
    }

    @Override
    public void generateBedrock(
            @NotNull WorldInfo worldInfo,
            @NotNull Random random,
            int chunkX, int chunkZ,
            @NotNull ChunkData chunkData) {

        int minY = chunkData.getMinHeight();

        chunkData.setRegion(0, minY, 0, 16, minY + 1, 16, Material.BEDROCK);
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }


    @Override
    public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        return new Location(world, 0.5, 64, 0.5);
    }

    public static Material parseMaterial(String name) {
        if (name == null || name.isBlank()) {
            return Material.WATER;
        }

        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Material.WATER;
        }
    }

}
