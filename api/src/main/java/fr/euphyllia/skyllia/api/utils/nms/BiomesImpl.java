package fr.euphyllia.skyllia.api.utils.nms;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BiomesImpl {

    public abstract @Nullable Biome getBiome(String biomeName);

    public abstract List<String> getBiomeNameList();

    public abstract String getNameBiome(Biome biome);

    public abstract boolean setBiome(World world, int chunkX, int chunkZ, Biome biome);
}
