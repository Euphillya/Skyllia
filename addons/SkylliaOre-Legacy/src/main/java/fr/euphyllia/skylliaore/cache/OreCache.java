package fr.euphyllia.skylliaore.cache;

import fr.euphyllia.skyllia.api.utils.ExpiringValue;
import fr.euphyllia.skylliaore.SkylliaOre;
import fr.euphyllia.skylliaore.api.Generator;
import fr.euphyllia.skylliaore.utils.OptimizedGenerator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class OreCache {

    private static final long TTL_GENERATOR_SEC = 10;
    private static final long TTL_OPTIMIZED_SEC = 60;

    private final ConcurrentHashMap<UUID, ExpiringValue<Generator>> generatorByIsland = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ExpiringValue<OptimizedGenerator>> optimizedByGeneratorName = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<UUID, CompletableFuture<Generator>> inFlightLoad = new ConcurrentHashMap<>();

    private static <K, V> @Nullable V getIfValid(ConcurrentHashMap<K, ExpiringValue<V>> map, K key) {
        ExpiringValue<V> ev = map.get(key);
        if (ev == null) return null;
        if (ev.isExpired()) {
            map.remove(key, ev);
            return null;
        }
        return ev.get();
    }

    private static <K, V> void put(ConcurrentHashMap<K, ExpiringValue<V>> map, K key, V value, long ttlSec) {
        map.put(key, ExpiringValue.of(value, ttlSec, TimeUnit.SECONDS));
    }

    public @Nullable Generator getGeneratorIfValid(UUID islandId) {
        return getIfValid(generatorByIsland, islandId);
    }

    public void putGenerator(UUID islandId, Generator generator) {
        put(generatorByIsland, islandId, generator, TTL_GENERATOR_SEC);
    }

    public Generator getGeneratorOrDefaultAndRefresh(
            Plugin plugin,
            UUID islandId,
            Supplier<Generator> dbLoaderSync,
            Generator defaultGenerator
    ) {
        Generator cached = getGeneratorIfValid(islandId);
        if (cached != null) return cached;

        refreshGeneratorAsync(plugin, islandId, dbLoaderSync);
        return defaultGenerator;
    }

    public void refreshGeneratorAsync(Plugin plugin, UUID islandId, Supplier<Generator> dbLoaderSync) {
        inFlightLoad.computeIfAbsent(islandId, id -> {
            CompletableFuture<Generator> future = new CompletableFuture<>();

            Bukkit.getAsyncScheduler().runNow(plugin, task -> {
                try {
                    Generator gen = dbLoaderSync.get();
                    if (gen == null) gen = SkylliaOre.getDefaultConfig().getDefaultGenerator();
                    putGenerator(islandId, gen);
                    future.complete(gen);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                } finally {
                    inFlightLoad.remove(id);
                }
            });

            return future;
        });
    }

    public void invalidateIsland(UUID islandId) {
        generatorByIsland.remove(islandId);
        inFlightLoad.remove(islandId);
    }

    public OptimizedGenerator getOrBuildOptimized(Generator generator) {
        String key = generator.name();

        OptimizedGenerator cached = getIfValid(optimizedByGeneratorName, key);
        if (cached != null) return cached;

        OptimizedGenerator built = new OptimizedGenerator(generator);
        put(optimizedByGeneratorName, key, built, TTL_OPTIMIZED_SEC);
        return built;
    }

    public void invalidateOptimized(String generatorName) {
        optimizedByGeneratorName.remove(generatorName);
    }

    public void clearAll() {
        generatorByIsland.clear();
        optimizedByGeneratorName.clear();
        inFlightLoad.clear();
    }
}
