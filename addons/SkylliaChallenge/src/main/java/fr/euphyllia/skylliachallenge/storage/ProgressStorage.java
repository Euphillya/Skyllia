package fr.euphyllia.skylliachallenge.storage;

import fr.euphyllia.skylliachallenge.SkylliaChallenge;
import org.bukkit.NamespacedKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressStorage {

    private static final Logger log = LoggerFactory.getLogger(ProgressStorage.class);

    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Integer>> CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<String, Long>> LAST_COMPLETED_CACHE = new ConcurrentHashMap<>();

    private static ExecutorService EXECUTOR;

    private ProgressStorage() {
    }

    public static void initExecutor(int threads) {
        if (threads <= 0) threads = 2;
        EXECUTOR = Executors.newFixedThreadPool(threads);
    }

    public static int getTimesCompleted(UUID islandId, NamespacedKey challengeId) {
        String cid = challengeId.asString();
        var m = CACHE.get(islandId);
        return (m != null) ? m.getOrDefault(cid, 0) : 0;
    }

    public static long getLastCompleted(UUID islandId, NamespacedKey challengeId) {
        String cid = challengeId.asString();
        var m = LAST_COMPLETED_CACHE.get(islandId);
        return (m != null) ? m.getOrDefault(cid, 0L) : 0L;
    }

    public static void preloadAllProgress() {
        log.debug("[SkylliaChallenge] Preloading challenge progress into memory...");

        var backend = SkylliaChallenge.getInstance().getProgressBackend();
        AtomicInteger count = new AtomicInteger(0);

        backend.preloadProgress(row -> {
            CACHE.computeIfAbsent(row.islandId(), k -> new ConcurrentHashMap<>())
                    .put(row.challengeId(), row.timesCompleted());
            LAST_COMPLETED_CACHE.computeIfAbsent(row.islandId(), k -> new ConcurrentHashMap<>())
                    .put(row.challengeId(), row.lastCompletedAt());

            int c = count.incrementAndGet();
            if (c % 10000 == 0) {
                log.debug("[SkylliaChallenge] Loaded {} challenge rows so far...", c);
            }
        });

        log.debug("[SkylliaChallenge] Finished preloading {} entries.", count.get());
    }

    public static void updateCompletion(UUID islandId, NamespacedKey challengeId, long nowEpochMillis) {
        String cid = challengeId.asString();

        CACHE.computeIfAbsent(islandId, k -> new ConcurrentHashMap<>())
                .merge(cid, 1, Integer::sum);
        LAST_COMPLETED_CACHE.computeIfAbsent(islandId, k -> new ConcurrentHashMap<>())
                .put(cid, nowEpochMillis);

        EXECUTOR.submit(() -> {
            try {
                SkylliaChallenge.getInstance().getProgressBackend()
                        .incrementCompletion(islandId, cid, nowEpochMillis);
            } catch (Throwable t) {
                log.error("Error updating completion: {}", t.getMessage(), t);
            }
        });
    }

    public static void shutdown() {
        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
        }

        var backend = SkylliaChallenge.getInstance().getProgressBackend();

        for (var islandEntry : CACHE.entrySet()) {
            UUID islandId = islandEntry.getKey();
            var timesMap = islandEntry.getValue();
            var lastMap = LAST_COMPLETED_CACHE.getOrDefault(islandId, new ConcurrentHashMap<>());

            for (var challengeEntry : timesMap.entrySet()) {
                String challengeId = challengeEntry.getKey();
                int times = challengeEntry.getValue();
                long last = lastMap.getOrDefault(challengeId, 0L);

                try {
                    backend.upsertProgressSet(islandId, challengeId, times, last);
                } catch (Throwable t) {
                    log.error("Error flushing progress on shutdown: {}", t.getMessage(), t);
                }
            }
        }
    }
}
