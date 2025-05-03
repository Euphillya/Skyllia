package fr.euphyllia.skyllia.utils.nms.v1_21_R2;

import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BiomeNMS extends BiomesImpl {

    private static final HashMap<Biome, Holder<net.minecraft.world.level.biome.Biome>> biomeTypeToNMSCache = new HashMap<>();
    private static final Logger log = LogManager.getLogger(BiomeNMS.class);

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

    @Override
    public boolean setBiome(World world, int chunkX, int chunkZ, Biome biome) {
        try {
            CraftWorld craftWorld = (CraftWorld) world;
            ServerLevel handle = craftWorld.getHandle();
            LevelChunk chunk = handle.getChunk(chunkX, chunkZ);

            var biomeHolder = biomeTypeToNMSCache.computeIfAbsent(biome, b -> ((CraftServer) Bukkit.getServer()).getServer().registryAccess()
                    .lookupOrThrow(Registries.BIOME)
                    .getOrThrow(ResourceKey.create(Registries.BIOME, ResourceLocation.parse(getNameBiome(biome))))
            );


            for (LevelChunkSection section : chunk.getSections()) {
                if (section == null) continue;

                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        for (int z = 0; z < 4; z++) {
                            section.setBiome(x, y, z, biomeHolder);
                        }
                    }
                }
            }

            chunk.markUnsaved();
            return true;
        } catch (Exception exception) {
            log.error("Failed to set biome", exception);
            return false;
        }
    }
}
