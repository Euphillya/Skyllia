package fr.euphyllia.skylliachallenge.storage;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.execute.MariaDBExecute;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import org.bukkit.NamespacedKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

public class ProgressStoragePartial {

    private static final Logger log = LoggerFactory.getLogger(ProgressStoragePartial.class);
    private static final ConcurrentHashMap<PartialKey, Long> CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService DB_EXECUTOR = Executors.newFixedThreadPool(2);

    private ProgressStoragePartial() {
    }

    static boolean useMaria() {
        return InitMariaDB.getPool() != null;
    }

    public static void shutdown() {
        for (var entry : CACHE.entrySet()) {
            try {
                writeToDatabase(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("Error flushing cache on shutdown: {}", e.getMessage(), e);
            }
        }

        DB_EXECUTOR.shutdown();
        try {
            if (!DB_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                DB_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            DB_EXECUTOR.shutdownNow();
        }
    }

    public static long getPartial(UUID islandId, NamespacedKey challengeId, int requirementId) {
        PartialKey key = new PartialKey(islandId, challengeId, requirementId);
        return CACHE.getOrDefault(key, 0L);
    }

    public static void addPartial(UUID islandId, NamespacedKey challengeId, int requirementId, long delta) {
        if (delta <= 0) return;

        PartialKey key = new PartialKey(islandId, challengeId, requirementId);

        CACHE.merge(key, delta, Long::sum);

        CompletableFuture.runAsync(() -> {
            try {
                writeToDatabase(key, delta);
            } catch (Exception e) {
                log.error("Error writing partial progress to database: {}", e.getMessage(), e);
            }
        }, DB_EXECUTOR);
    }

    private static void writeToDatabase(PartialKey key, long delta) throws DatabaseException {
        if (useMaria()) {
            String q = """
                    INSERT INTO `%s`.`island_challenge_partial` 
                    (island_id, challenge_id, requirement_id, collected_amount)
                    VALUES(?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE collected_amount = collected_amount + VALUES(collected_amount);
                    """.formatted(InitMariaDB.databaseName());
            MariaDBExecute.executeQueryDML(
                    InitMariaDB.getPool(),
                    q,
                    List.of(key.islandId(), key.challengeId().asString(), key.requirementId(), delta),
                    null,
                    null
            );
        } else {
            String q = """
                    INSERT INTO island_challenge_partial 
                    (island_id, challenge_id, requirement_id, collected_amount)
                    VALUES(?, ?, ?, ?)
                    ON CONFLICT(island_id, challenge_id, requirement_id)
                    DO UPDATE SET collected_amount = collected_amount + excluded.collected_amount;
                    """;
            SQLiteDatabaseLoader db = InitSQLite.getPool();
            db.executeUpdate(
                    q,
                    List.of(key.islandId().toString(), key.challengeId().asString(), key.requirementId(), delta),
                    null,
                    null
            );
        }
    }

    public record PartialKey(UUID islandId, NamespacedKey challengeId, int requirementId) {
    }
}
