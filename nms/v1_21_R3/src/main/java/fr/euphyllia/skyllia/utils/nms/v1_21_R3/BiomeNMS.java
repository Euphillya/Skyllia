package fr.euphyllia.skyllia.utils.nms.v1_21_R3;

import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BiomeNMS extends BiomesImpl {

    @Override
    public @Nullable Biome getBiome(String biomeName) {
        biomeName = biomeName.trim().toLowerCase(Locale.ROOT);
        var biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);

        if (biomeName.contains(":")) {
            NamespacedKey key = NamespacedKey.fromString(biomeName);
            if (key != null) {
                Biome biome = biomeRegistry.get(key);
                if (biome != null) return biome;
            }
        }

        Biome biome = biomeRegistry.get(NamespacedKey.minecraft(biomeName));
        if (biome != null) return biome;

        try {
            return Biome.valueOf(biomeName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    @Override
    public List<String> getBiomeNameList() {
        Registry<Biome> biomeRegistry = io.papermc.paper.registry.RegistryAccess.registryAccess()
                .getRegistry(io.papermc.paper.registry.RegistryKey.BIOME);

        List<String> list = new ArrayList<>();
        for (Biome biome : biomeRegistry) {
            NamespacedKey key = biome.getKey();
            list.add(key.toString());
        }
        return list;
    }


    @Override
    public String getNameBiome(Biome biome) {
        NamespacedKey key = biome.getKey();
        return key != null ? key.toString() : "unknown";
    }

}
