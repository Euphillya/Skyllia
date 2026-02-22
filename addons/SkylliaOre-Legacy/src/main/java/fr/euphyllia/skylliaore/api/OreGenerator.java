package fr.euphyllia.skylliaore.api;

import java.util.UUID;

public interface OreGenerator {
    Generator getGenIsland(UUID islandId);

    Boolean updateGenIsland(UUID islandId, String generatorName);
}
