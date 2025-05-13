package fr.euphyllia.skylliaore.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface OreGenerator {
    CompletableFuture<Generator> getGenIsland(UUID islandId);

    CompletableFuture<Boolean> updateGenIsland(UUID islandId, String generatorName);
}
