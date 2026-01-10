package fr.euphyllia.skylliachallenge.storage;

import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import org.bukkit.NamespacedKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.*;

public class ProgressStoragePartial {

    private static final Logger log = LoggerFactory.getLogger(ProgressStoragePartial.class);

    private static final ConcurrentHashMap<PartialKey, Long> CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<PartialKey> DIRTY_KEYS = new ConcurrentLinkedQueue<>();

    private static ExecutorService DB_EXECUTOR;

    private ProgressStoragePartial() {
    }

    public static void initExecutor(int threads) {
        if (threads <= 0) threads = 2;
        DB_EXECUTOR = Executors.newFixedThreadPool(threads);
    }

    public static long getPartial(UUID islandId, NamespacedKey challengeId, int requirementId) {
        return CACHE.getOrDefault(new PartialKey(islandId, challengeId, requirementId), 0L);
    }

    public static void addPartial(UUID islandId, NamespacedKey challengeId, int requirementId, long delta) {
        if (delta <= 0) return;

        PartialKey key = new PartialKey(islandId, challengeId, requirementId);
        CACHE.merge(key, delta, Long::sum);
        DIRTY_KEYS.add(key);
    }

    public static void flushDirty() {
        PartialKey key;
        while ((key = DIRTY_KEYS.poll()) != null) {
            final PartialKey partialKey = key;
            Long value = CACHE.get(partialKey);
            if (value == null) continue;

            long snapshot = value;
            CompletableFuture.runAsync(() -> {
                try {
                    SkylliaChallenge.getInstance().getProgressBackend()
                            .setPartial(
                                    partialKey.islandId(),
                                    partialKey.challengeId().asString(),
                                    partialKey.requirementId(),
                                    snapshot
                            );
                } catch (Throwable t) {
                    log.error("Error flushing partial progress: {}", t.getMessage(), t);
                }
            }, DB_EXECUTOR);
        }
    }

    public static void flushAll() {
        var backend = SkylliaChallenge.getInstance().getProgressBackend();
        for (var entry : CACHE.entrySet()) {
            try {
                PartialKey k = entry.getKey();
                backend.setPartial(k.islandId(), k.challengeId().asString(), k.requirementId(), entry.getValue());
            } catch (Throwable t) {
                log.error("Error flushing all partial on shutdown: {}", t.getMessage(), t);
            }
        }
    }

    public static void shutdown() {
        flushAll();

        DB_EXECUTOR.shutdown();
        try {
            if (!DB_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                DB_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            DB_EXECUTOR.shutdownNow();
        }
    }

    public static void resetPartial(UUID islandId, NamespacedKey challengeId) {
        CACHE.keySet().removeIf(k -> k.islandId().equals(islandId) && k.challengeId().equals(challengeId));
        DIRTY_KEYS.removeIf(k -> k.islandId().equals(islandId) && k.challengeId().equals(challengeId));

        DB_EXECUTOR.submit(() -> {
            try {
                SkylliaChallenge.getInstance().getProgressBackend()
                        .deletePartialForChallenge(islandId, challengeId.asString());
            } catch (Throwable t) {
                log.error("Error resetting partial progress: {}", t.getMessage(), t);
            }
        });
    }

    public static void preloadAllPartialProgress() {
        var backend = SkylliaChallenge.getInstance().getProgressBackend();
        backend.preloadPartial(row -> {
            UUID islandId = row.islandId();
            NamespacedKey challengeKey = NamespacedKey.fromString(row.challengeId());
            if (challengeKey == null) return;

            CACHE.put(new PartialKey(islandId, challengeKey, row.requirementId()), row.collectedAmount());
        });
    }

    public record PartialKey(UUID islandId, NamespacedKey challengeId, int requirementId) {
    }
}
