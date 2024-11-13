package fr.euphyllia.skyllia.api.utils.nms;

import org.bukkit.block.Biome;

import java.util.List;

public abstract class BiomesImpl {

    public abstract Biome getBiome(String biomeName);

    public abstract List<String> getBiomeNameList();

    public abstract String getNameBiome(Biome biome);

}
