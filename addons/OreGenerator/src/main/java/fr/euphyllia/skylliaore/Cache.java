package fr.euphyllia.skylliaore;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.database.MariaDBInit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Cache {

    private static final Logger logger = LogManager.getLogger(Cache.class);

    private final LoadingCache<UUID, Generator> generatorLoadingCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build(
                    new CacheLoader<>() {
                        @Override
                        public @NotNull Generator load(@NotNull UUID cache) {
                            try {
                                return MariaDBInit.getMariaDbGenerator().getGenIsland(cache).get(2, TimeUnit.SECONDS);
                            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                                logger.error(e.getMessage(), e);
                                return Main.getDefaultConfig().getDefaultGenerator();
                            }
                        }
                    }
            );

    public Cache() {

    }

    public Generator getGeneratorIsland(UUID island) {
        try {
            return generatorLoadingCache.get(island);
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
            return Main.getDefaultConfig().getDefaultGenerator();
        }
    }
}
