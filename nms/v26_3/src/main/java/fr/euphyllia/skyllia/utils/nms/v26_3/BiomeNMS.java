package fr.euphyllia.skyllia.utils.nms.v26_3;

import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
        final var biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);

        if (biomeName.contains(":")) {
            final NamespacedKey key = NamespacedKey.fromString(biomeName);
            if (key != null) {
                final Biome biome = biomeRegistry.get(key);
                if (biome != null) return biome;
            }
        }

        final Biome biome = biomeRegistry.get(NamespacedKey.minecraft(biomeName));
        if (biome != null) return biome;

        return Bukkit.getUnsafe().get(RegistryKey.BIOME, NamespacedKey.fromString(biomeName.toLowerCase(Locale.ROOT)));
    }

    @Override
    public List<String> getBiomeNameList() {
        final Registry<@org.jetbrains.annotations.NotNull Biome> biomeRegistry = io.papermc.paper.registry.RegistryAccess.registryAccess()
                .getRegistry(io.papermc.paper.registry.RegistryKey.BIOME);

        final List<String> list = new ArrayList<>();
        for (final Biome biome : biomeRegistry) {
            final NamespacedKey key = biome.getKey();
            list.add(key.toString());
        }
        return list;
    }

    @Override
    public String getNameBiome(final Biome biome) {
        final NamespacedKey key = biome.getKey();
        return key != null ? key.toString() : "unknown";
    }

    @Override
    public boolean setBiome(final World world, final int chunkX, final int chunkZ, final Biome biome) {
        try {
            final net.minecraft.server.level.ServerLevel nms = ((CraftWorld) world).getHandle();

            LevelChunk chunk = nms.getChunkSource().getChunkNow(chunkX, chunkZ);

            if (chunk == null) {
                chunk = nms.getChunkSource().getChunk(chunkX, chunkZ, true);
            }

            if (chunk == null) {
                return false;
            }

            final LevelChunkSection[] sections = chunk.getSections();
            if (sections.length == 0) {
                return false;
            }

            final var biomeHolder = biomeTypeToNMSCache.computeIfAbsent(biome, b -> ((CraftServer) Bukkit.getServer()).getServer().registryAccess()
                    .lookupOrThrow(Registries.BIOME)
                    .getOrThrow(ResourceKey.create(Registries.BIOME, Identifier.parse(getNameBiome(biome))))
            );

            for (final LevelChunkSection section : sections) {
                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        for (int z = 0; z < 4; z++) {
                            section.setNoiseBiome(x, y, z, biomeHolder);
                        }
                    }
                }
            }

            chunk.markUnsaved();
            return true;
        } catch (final Exception exception) {
            log.error("Failed to set biome", exception);
            return false;
        }
    }
}
