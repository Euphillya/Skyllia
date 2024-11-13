package fr.euphyllia.skyllia.utils.nms.v1_20_R1;

import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;

public class BiomeNMS extends BiomesImpl {

    @Override
    public Biome getBiome(String biomeName) {
        return Biome.valueOf(biomeName);
    }

    @Override
    public List<String> getBiomeNameList() {
        List<String> list = new ArrayList<>();
        for (Biome biome : Registry.BIOME) {
            list.add(biome.name());
        }
        return list;
    }

    @Override
    public String getNameBiome(Biome biome) {
        return biome.name();
    }
}
