package fr.euphyllia.skylliabank.cache;

import fr.euphyllia.skyllia.api.utils.ExpiringValue;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class BankPapiCache {

    private static final long TTL_SEC = 2;

    private final ConcurrentHashMap<UUID, ExpiringValue<Double>> balanceByIsland = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, CompletableFuture<Double>> inFlight = new ConcurrentHashMap<>();

    private static <K, V> @Nullable V getIfValid(ConcurrentHashMap<K, ExpiringValue<V>> map, K key) {
        ExpiringValue<V> ev = map.get(key);
        if (ev == null) return null;
        if (ev.isExpired()) {
            map.remove(key, ev);
            return null;
        }
        return ev.get();
    }

    public @Nullable Double getBalanceIfValid(UUID islandId) {
        return getIfValid(balanceByIsland, islandId);
    }

    public void putBalance(UUID islandId, double balance) {
        balanceByIsland.put(islandId, ExpiringValue.of(balance, TTL_SEC, TimeUnit.SECONDS));
    }

    public void invalidate(UUID islandId) {
        balanceByIsland.remove(islandId);
        inFlight.remove(islandId);
    }

    public void clear() {
        balanceByIsland.clear();
        inFlight.clear();
    }

    public double getBalanceOrDefaultAndRefresh(
            Plugin plugin,
            UUID islandId,
            Supplier<Double> dbLoaderSync,
            double fallback
    ) {
        Double cached = getBalanceIfValid(islandId);
        if (cached != null) return cached;

        refreshAsync(plugin, islandId, dbLoaderSync);
        return fallback;
    }

    public void refreshAsync(Plugin plugin, UUID islandId, Supplier<Double> dbLoaderSync) {
        inFlight.computeIfAbsent(islandId, id -> {
            CompletableFuture<Double> f = new CompletableFuture<>();
            Bukkit.getAsyncScheduler().runNow(plugin, task -> {
                try {
                    Double bal = dbLoaderSync.get();
                    if (bal != null) putBalance(islandId, bal);
                    f.complete(bal);
                } catch (Throwable t) {
                    f.completeExceptionally(t);
                } finally {
                    inFlight.remove(id);
                }
            });
            return f;
        });
    }
}
