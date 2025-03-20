package fr.euphyllia.skyllia.utils.nms.v1_21_R4;

import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;

public class BiomeNMS extends BiomesImpl {

    @Override
    public Biome getBiome(String biomeName) {
        for (Biome biome : RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME)) {
            if (biome.getKey().getKey().equalsIgnoreCase(biomeName)) return biome;
        }
        return Biome.PLAINS;
    }

    @Override
    public List<String> getBiomeNameList() {
        List<String> list = new ArrayList<>();
        for (Biome biome : RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME)) {
            list.add(biome.getKey().getKey());
        }
        return list;
    }

    @Override
    public String getNameBiome(Biome biome) {
        return biome.getKey().getKey();
    }
}
