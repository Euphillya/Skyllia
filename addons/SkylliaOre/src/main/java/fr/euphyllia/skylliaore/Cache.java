package fr.euphyllia.skylliaore;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.database.MariaDBInit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class Cache {

    private static final Logger logger = LogManager.getLogger(Cache.class);

    private final AsyncLoadingCache<UUID, Generator> generatorLoadingCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .buildAsync((islandId, executor) -> {
                CompletableFuture<Generator> future = MariaDBInit.getMariaDbGenerator().getGenIsland(islandId);
                return future.exceptionally(e -> {
                    logger.error("Error loading generator for island {}: {}", islandId, e.getMessage(), e);
                    return SkylliaOre.getDefaultConfig().getDefaultGenerator();
                });
            });

    public Cache() {

    }

    public Generator getGeneratorIsland(UUID islandId) {
        try {
            return generatorLoadingCache.get(islandId).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Réinitialiser le statut d'interruption
            logger.error("Thread interrupted while accessing cache for island {}: {}", islandId, e.getMessage(), e);
            return SkylliaOre.getDefaultConfig().getDefaultGenerator();
        } catch (ExecutionException e) {
            logger.error("Error accessing cache for island {}: {}", islandId, e.getMessage(), e);
            return SkylliaOre.getDefaultConfig().getDefaultGenerator();
        }
    }
}
