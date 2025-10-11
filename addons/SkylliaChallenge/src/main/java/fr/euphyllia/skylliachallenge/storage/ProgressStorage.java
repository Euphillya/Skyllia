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

    public static void increment(UUID islandId, NamespacedKey challengeId) {
        String cid = challengeId.asString();

        CACHE.computeIfAbsent(islandId, k -> new ConcurrentHashMap<>())
                .merge(cid, 1, Integer::sum);

        EXECUTOR.submit(() -> {
            try {
                if (useMaria()) {
                    String q = """
                            INSERT INTO `%s`.`island_challenge_progress` (island_id, challenge_id, times_completed)
                            VALUES(?, ?, 1)
                            ON DUPLICATE KEY UPDATE times_completed = times_completed + 1;
                            """.formatted(InitMariaDB.databaseName());
                    MariaDBExecute.executeQueryDML(InitMariaDB.getPool(), q, List.of(islandId, cid), null, null);
                } else {
                    String q = """
                            INSERT INTO island_challenge_progress (island_id, challenge_id, times_completed)
                            VALUES(?, ?, 1)
                            ON CONFLICT(island_id, challenge_id) DO UPDATE SET times_completed = times_completed + 1;
                            """;
                    InitSQLite.getPool().executeUpdate(q, List.of(islandId.toString(), cid), null, null);
                }
            } catch (DatabaseException e) {
                log.error("Error incrementing progress: {}", e.getMessage());
            }
        });
    }

    public static void shutdown() {
        for (var islandEntry : CACHE.entrySet()) {
            UUID islandId = islandEntry.getKey();
            for (var challengeEntry : islandEntry.getValue().entrySet()) {
                String challengeId = challengeEntry.getKey();
                int value = challengeEntry.getValue();

                try {
                    if (useMaria()) {
                        String q = """
                        INSERT INTO `%s`.`island_challenge_progress` (island_id, challenge_id, times_completed)
                        VALUES(?, ?, ?)
                        ON DUPLICATE KEY UPDATE times_completed = VALUES(times_completed);
                    """.formatted(InitMariaDB.databaseName());
                        MariaDBExecute.executeQueryDML(
                                InitMariaDB.getPool(),
                                q,
                                List.of(islandId, challengeId, value),
                                null,
                                null
                        );
                    } else {
                        String q = """
                        INSERT INTO island_challenge_progress (island_id, challenge_id, times_completed)
                        VALUES(?, ?, ?)
                        ON CONFLICT(island_id, challenge_id) DO UPDATE SET times_completed = excluded.times_completed;
                    """;
                        InitSQLite.getPool().executeUpdate(
                                q,
                                List.of(islandId.toString(), challengeId, value),
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
}