package fr.euphyllia.skylliaore;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.api.OreGenerator;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GeneratorManager {

    private final OreGenerator dbGenerator;
    private final Cache<UUID, Generator> cache;

    public GeneratorManager(OreGenerator dbGenerator) {
        this.dbGenerator = dbGenerator;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .maximumSize(ConfigLoader.general.getMaxIslands())
                .build();
    }

    public CompletableFuture<Generator> getGenerator(UUID islandId) {
        Generator cached = cache.getIfPresent(islandId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return dbGenerator.getGenIsland(islandId).thenApply(generator -> {
            cache.put(islandId, generator);
            return generator;
        });
    }

    public CompletableFuture<Boolean> updateGenerator(UUID islandId, String generatorId) {
        return dbGenerator.updateGenIsland(islandId, generatorId).thenApply(success -> {
            if (success) {
                Generator newGen = SkylliaOre.getDefaultConfig().getGenerators().getOrDefault(generatorId,
                        SkylliaOre.getDefaultConfig().getDefaultGenerator());
                cache.put(islandId, newGen);
            }
            return success;
        });
    }

    public void invalidate(UUID islandId) {
        cache.invalidate(islandId);
    }
}
