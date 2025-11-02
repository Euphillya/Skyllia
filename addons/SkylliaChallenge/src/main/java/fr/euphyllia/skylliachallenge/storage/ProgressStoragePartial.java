package fr.euphyllia.skylliachallenge.storage;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.execute.SQLExecute;
import org.bukkit.NamespacedKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Optimized partial-progress storage using write-back cache with periodic flushing.
 */
public class ProgressStoragePartial {

    private static final Logger log = LoggerFactory.getLogger(ProgressStoragePartial.class);

    /**
     * In-memory cache: (islandId, challengeId, requirementId) -> value
     */
    private static final ConcurrentHashMap<PartialKey, Long> CACHE = new ConcurrentHashMap<>();

    /**
     * Keys that have been modified and must be flushed to DB
     */
    private static final ConcurrentLinkedQueue<PartialKey> DIRTY_KEYS = new ConcurrentLinkedQueue<>();

    /**
     * Background thread pool for DB operations
     */
    private static ExecutorService DB_EXECUTOR;

    private ProgressStoragePartial() {
    }

    public static void initExecutor(int threads) {
        if (threads <= 0) threads = 2;
        DB_EXECUTOR = Executors.newFixedThreadPool(threads);
    }

    static boolean useMaria() {
        return InitMariaDB.getPool() != null;
    }

    /**
     * Called on plugin disable — flushes everything to DB.
     */
    public static void shutdown() {
        flushAll(); // Flush everything manually

        DB_EXECUTOR.shutdown();
        try {
            if (!DB_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                DB_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            DB_EXECUTOR.shutdownNow();
        }
    }

    /**
     * Retrieve the current cached value.
     */
    public static long getPartial(UUID islandId, NamespacedKey challengeId, int requirementId) {
        PartialKey key = new PartialKey(islandId, challengeId, requirementId);
        return CACHE.getOrDefault(key, 0L);
    }

    /**
     * Add to the current partial progress — does NOT write immediately.
     */
    public static void addPartial(UUID islandId, NamespacedKey challengeId, int requirementId, long delta) {
        if (delta <= 0) return;

        PartialKey key = new PartialKey(islandId, challengeId, requirementId);

        CACHE.merge(key, delta, Long::sum);
        DIRTY_KEYS.add(key); // Mark as dirty
    }

    /**
     * Flush dirty entries to DB (only the latest values).
     * Can be scheduled periodically.
     */
    public static void flushDirty() {
        PartialKey key;
        while ((key = DIRTY_KEYS.poll()) != null) {
            final PartialKey partialKey = key;
            Long value = CACHE.get(key);
            if (value != null) {
                long snapshot = value;
                CompletableFuture.runAsync(() -> {
                    try {
                        writeSetToDatabase(partialKey, snapshot);
                    } catch (Exception e) {
                        log.error("Error flushing partial progress: {}", e.getMessage(), e);
                    }
                }, DB_EXECUTOR);
            }
        }
    }

    /**
     * Flush absolutely everything (used in shutdown).
     */
    public static void flushAll() {
        for (var entry : CACHE.entrySet()) {
            try {
                writeSetToDatabase(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("Error flushing all progress on shutdown: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Writes ABSOLUTE value (SET instead of ADD).
     */
    private static void writeSetToDatabase(PartialKey key, long newValue) throws DatabaseException {
        if (useMaria()) {
            String q = """
                    INSERT INTO `%s`.`island_challenge_partial` 
                    (island_id, challenge_id, requirement_id, collected_amount)
                    VALUES(?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE collected_amount = VALUES(collected_amount);
                    """.formatted(InitMariaDB.databaseName());
            SQLExecute.executeQueryDML(
                    InitMariaDB.getPool(),
                    q,
                    List.of(key.islandId(), key.challengeId().asString(), key.requirementId(), newValue),
                    null,
                    null
            );
        } else {
            String q = """
                    INSERT INTO island_challenge_partial 
                    (island_id, challenge_id, requirement_id, collected_amount)
                    VALUES(?, ?, ?, ?)
                    ON CONFLICT(island_id, challenge_id, requirement_id)
                    DO UPDATE SET collected_amount = excluded.collected_amount;
                    """;
            SQLiteDatabaseLoader db = InitSQLite.getPool();
            db.executeUpdate(
                    q,
                    List.of(key.islandId().toString(), key.challengeId().asString(), key.requirementId(), newValue),
                    null,
                    null
            );
        }
    }

    public static void resetPartial(UUID islandId, NamespacedKey challengeId) {
        CACHE.keySet().removeIf(key ->
                key.islandId().equals(islandId) && key.challengeId().equals(challengeId)
        );

        DIRTY_KEYS.removeIf(key ->
                key.islandId().equals(islandId) && key.challengeId().equals(challengeId)
        );

        DB_EXECUTOR.submit(() -> {
            try {
                if (useMaria()) {
                    String q = """
                                DELETE FROM `%s`.`island_challenge_partial`
                                WHERE island_id = ? AND challenge_id = ?;
                            """.formatted(InitMariaDB.databaseName());
                    SQLExecute.executeQueryDML(
                            InitMariaDB.getPool(),
                            q,
                            List.of(islandId, challengeId.asString()),
                            null,
                            null
                    );
                } else {
                    String q = """
                                DELETE FROM island_challenge_partial
                                WHERE island_id = ? AND challenge_id = ?;
                            """;
                    SQLiteDatabaseLoader db = InitSQLite.getPool();
                    db.executeUpdate(
                            q,
                            List.of(islandId.toString(), challengeId.asString()),
                            null,
                            null
                    );
                }
            } catch (DatabaseException e) {
                log.error("Error resetting partial progress in DB: {}", e.getMessage(), e);
            }
        });
    }

    public static void preloadAllPartialProgress() {
        try {
            if (useMaria()) {
                String q = "SELECT island_id, challenge_id, requirement_id, collected_amount FROM `%s`.`island_challenge_partial`;"
                        .formatted(InitMariaDB.databaseName());
                SQLExecute.executeQuery(InitMariaDB.getPool(), q, null, rs -> {
                    try {
                        while (rs != null && rs.next()) {
                            UUID islandId = UUID.fromString(rs.getString("island_id"));
                            NamespacedKey challengeId = NamespacedKey.fromString(rs.getString("challenge_id"));
                            int reqId = rs.getInt("requirement_id");
                            long value = rs.getLong("collected_amount");

                            CACHE.put(new PartialKey(islandId, challengeId, reqId), value);
                        }
                    } catch (SQLException ignored) {
                    }
                }, null);
            } else {
                String q = "SELECT island_id, challenge_id, requirement_id, collected_amount FROM island_challenge_partial;";
                InitSQLite.getPool().executeQuery(q, null, rs -> {
                    try {
                        while (rs != null && rs.next()) {
                            UUID islandId = UUID.fromString(rs.getString("island_id"));
                            NamespacedKey challengeId = NamespacedKey.fromString(rs.getString("challenge_id"));
                            int reqId = rs.getInt("requirement_id");
                            long value = rs.getLong("collected_amount");

                            CACHE.put(new PartialKey(islandId, challengeId, reqId), value);
                        }
                    } catch (SQLException ignored) {
                    }
                }, null);
            }
        } catch (DatabaseException e) {
            log.error("Error preloading partial progress: {}", e.getMessage());
        }
    }


    public record PartialKey(UUID islandId, NamespacedKey challengeId, int requirementId) {
    }
}
