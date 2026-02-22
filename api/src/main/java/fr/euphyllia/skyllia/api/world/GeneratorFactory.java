package fr.euphyllia.skyllia.api.world;

import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import org.bukkit.generator.ChunkGenerator;

@FunctionalInterface
public interface GeneratorFactory {
    ChunkGenerator create(String worldName, WorldConfig config);
}