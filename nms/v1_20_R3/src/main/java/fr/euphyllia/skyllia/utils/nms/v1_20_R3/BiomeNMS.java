package fr.euphyllia.skyllia.utils.nms.v1_20_R3;

import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
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
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BiomeNMS extends BiomesImpl {

    private static final HashMap<Biome, Holder<net.minecraft.world.level.biome.Biome>> biomeTypeToNMSCache = new HashMap<>();
    private static final Logger log = LogManager.getLogger(BiomeNMS.class);

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

    @Override
    public boolean setBiome(World world, int chunkX, int chunkZ, Biome biome) {
        try {
            CraftWorld craftWorld = (CraftWorld) world;
            ServerLevel handle = craftWorld.getHandle();
            LevelChunk chunk = handle.getChunk(chunkX, chunkZ);

            var biomeHolder = biomeTypeToNMSCache.computeIfAbsent(biome, b -> ((CraftServer) Bukkit.getServer()).getServer().registryAccess()
                    .lookupOrThrow(Registries.BIOME)
                    .getOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation(getNameBiome(biome))))
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

            chunk.setUnsaved(true);
            return true;
        } catch (Exception exception) {
            log.error("Failed to set biome", exception);
            return false;
        }
    }

}
