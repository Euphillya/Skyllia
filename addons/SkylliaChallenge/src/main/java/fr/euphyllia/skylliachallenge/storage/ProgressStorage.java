package fr.euphyllia.skylliachallenge.storage;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.execute.MariaDBExecute;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import org.bukkit.NamespacedKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
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

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    private ProgressStorage() {
    }

    static boolean useMaria() {
        return InitMariaDB.getPool() != null;
    }

    public static int getTimesCompleted(UUID islandId, NamespacedKey challengeId) {
        String cid = challengeId.asString();

        var m = CACHE.get(islandId);
        if (m != null && m.containsKey(cid)) {
            return m.get(cid);
        }

        AtomicInteger res = new AtomicInteger(0);
        try {
            if (useMaria()) {
                String q = "SELECT times_completed FROM `%s`.`island_challenge_progress` WHERE island_id=? AND challenge_id=?;"
                        .formatted(InitMariaDB.databaseName());
                MariaDBExecute.executeQuery(InitMariaDB.getPool(), q, List.of(islandId, cid), rs -> {
                    try {
                        if (rs != null && rs.next()) res.set(rs.getInt("times_completed"));
                    } catch (SQLException ignored) {
                    }
                }, null);
            } else {
                String q = "SELECT times_completed FROM island_challenge_progress WHERE island_id=? AND challenge_id=?;";
                SQLiteDatabaseLoader db = InitSQLite.getPool();
                db.executeQuery(q, List.of(islandId.toString(), cid), rs -> {
                    try {
                        if (rs != null && rs.next()) res.set(rs.getInt("times_completed"));
                    } catch (SQLException ignored) {
                    }
                }, null);
            }
        } catch (DatabaseException e) {
            log.error("Error fetching progress: {}", e.getMessage());
        }

        CACHE.computeIfAbsent(islandId, k -> new ConcurrentHashMap<>()).put(cid, res.get());
        return res.get();
    }

    public static long getLastCompleted(UUID islandId, NamespacedKey challengeId) {
        String cid = challengeId.asString();

        var m = LAST_COMPLETED_CACHE.get(islandId);
        if (m != null && m.containsKey(cid)) {
            return m.get(cid);
        }

        final long[] result = {0L};
        try {
            if (useMaria()) {
                String q = "SELECT last_completed_at FROM `%s`.`island_challenge_progress` WHERE island_id=? AND challenge_id=?;"
                        .formatted(InitMariaDB.databaseName());
                MariaDBExecute.executeQuery(InitMariaDB.getPool(), q, List.of(islandId, cid), rs -> {
                    try {
                        if (rs != null && rs.next()) result[0] = rs.getLong("last_completed_at");
                    } catch (SQLException ignored) {
                    }
                }, null);
            } else {
                String q = "SELECT last_completed_at FROM island_challenge_progress WHERE island_id=? AND challenge_id=?;";
                InitSQLite.getPool().executeQuery(q, List.of(islandId.toString(), cid), rs -> {
                    try {
                        if (rs != null && rs.next()) result[0] = rs.getLong("last_completed_at");
                    } catch (SQLException ignored) {
                    }
                }, null);
            }
        } catch (DatabaseException e) {
            log.error("Error fetching last_completed_at: {}", e.getMessage());
        }

        LAST_COMPLETED_CACHE.computeIfAbsent(islandId, k -> new ConcurrentHashMap<>()).put(cid, result[0]);
        return result[0];
    }

    public static void shutdown() {
        for (var islandEntry : CACHE.entrySet()) {
            UUID islandId = islandEntry.getKey();
            var timesMap = islandEntry.getValue();
            var lastMap = LAST_COMPLETED_CACHE.getOrDefault(islandId, new ConcurrentHashMap<>());

            for (var challengeEntry : timesMap.entrySet()) {
                String challengeId = challengeEntry.getKey();
                int value = challengeEntry.getValue();
                long last = lastMap.getOrDefault(challengeId, 0L);

                try {
                    if (useMaria()) {
                        String q = """
                                INSERT INTO `%s`.`island_challenge_progress` (island_id, challenge_id, times_completed, last_completed_at)
                                VALUES(?, ?, ?, ?)
                                ON DUPLICATE KEY UPDATE
                                    times_completed = VALUES(times_completed),
                                    last_completed_at = GREATEST(VALUES(last_completed_at), last_completed_at);
                                """.formatted(InitMariaDB.databaseName());
                        MariaDBExecute.executeQueryDML(
                                InitMariaDB.getPool(),
                                q,
                                List.of(islandId, challengeId, value, last),
                                null,
                                null
                        );
                    } else {
                        String q = """
                                INSERT INTO island_challenge_progress (island_id, challenge_id, times_completed, last_completed_at)
                                VALUES(?, ?, ?, ?)
                                ON CONFLICT(island_id, challenge_id) DO UPDATE SET
                                    times_completed = excluded.times_completed,
                                    last_completed_at = MAX(excluded.last_completed_at, last_completed_at);
                                """;
                        InitSQLite.getPool().executeUpdate(
                                q,
                                List.of(islandId.toString(), challengeId, value, last),
                                null,
                                null
                        );
                    }
                } catch (DatabaseException e) {
                    log.error("Error flushing final challenge progress: {}", e.getMessage());
                }
            }
        }

        EXECUTOR.shutdown();
        try {
            if (!EXECUTOR.awaitTermination(5, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
        }
    }

    public static void updateCompletion(UUID islandId, NamespacedKey challengeId, long nowEpochMillis) {
        String cid = challengeId.asString();

        // Caches
        CACHE.computeIfAbsent(islandId, k -> new ConcurrentHashMap<>())
                .merge(cid, 1, Integer::sum);
        LAST_COMPLETED_CACHE.computeIfAbsent(islandId, k -> new ConcurrentHashMap<>())
                .put(cid, nowEpochMillis);

        // DB async
        EXECUTOR.submit(() -> {
            try {
                if (useMaria()) {
                    String q = """
                            INSERT INTO `%s`.`island_challenge_progress` (island_id, challenge_id, times_completed, last_completed_at)
                            VALUES(?, ?, 1, ?)
                            ON DUPLICATE KEY UPDATE
                                times_completed = times_completed + 1,
                                last_completed_at = VALUES(last_completed_at);
                            """.formatted(InitMariaDB.databaseName());
                    MariaDBExecute.executeQueryDML(InitMariaDB.getPool(), q,
                            List.of(islandId, cid, nowEpochMillis), null, null);
                } else {
                    String q = """
                            INSERT INTO island_challenge_progress (island_id, challenge_id, times_completed, last_completed_at)
                            VALUES(?, ?, 1, ?)
                            ON CONFLICT(island_id, challenge_id) 
                            DO UPDATE SET 
                                times_completed = times_completed + 1,
                                last_completed_at = excluded.last_completed_at;
                            """;
                    InitSQLite.getPool().executeUpdate(q,
                            List.of(islandId.toString(), cid, nowEpochMillis), null, null);
                }
            } catch (DatabaseException e) {
                log.error("Error updating completion: {}", e.getMessage());
            }
        });
    }
}