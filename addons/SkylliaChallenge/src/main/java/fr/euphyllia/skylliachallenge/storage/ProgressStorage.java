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
import java.util.concurrent.atomic.AtomicInteger;

public class ProgressStorage {

    private static final Logger log = LoggerFactory.getLogger(ProgressStorage.class);

    private ProgressStorage() {
    }

    static boolean useMaria() {
        return InitMariaDB.getPool() != null;
    }

    public static int getTimesCompleted(UUID islandId, NamespacedKey challengeId) {
        String id = challengeId.asString();
        AtomicInteger res = new AtomicInteger(0);
        try {
            if (useMaria()) {
                String q = "SELECT times_completed FROM `%s`.`island_challenge_progress` WHERE island_id=? AND challenge_id=?;"
                        .formatted(InitMariaDB.databaseName());
                MariaDBExecute.executeQuery(InitMariaDB.getPool(), q, List.of(islandId, id), rs -> {
                    try {
                        if (rs != null && rs.next()) res.set(rs.getInt("times_completed"));
                    } catch (
                            SQLException ignored) {
                    }
                }, null);
            } else {
                String q = "SELECT times_completed FROM island_challenge_progress WHERE island_id=? AND challenge_id=?;";
                SQLiteDatabaseLoader db = InitSQLite.getPool();
                db.executeQuery(q, List.of(islandId.toString(), id), rs -> {
                    try {
                        if (rs != null && rs.next()) res.set(rs.getInt("times_completed"));
                    } catch (SQLException ignored) {
                    }
                }, null);
            }
        } catch (DatabaseException e) {
            log.error("An error occurred while fetching challenge progress: {}", e.getMessage(), e);
        }
        return res.get();
    }

    public static void increment(UUID islandId, NamespacedKey challengeId) {
        String id = challengeId.asString();
        try {
            if (useMaria()) {
                String q = """
                        INSERT INTO `%s`.`island_challenge_progress` (island_id, challenge_id, times_completed)
                        VALUES(?, ?, 1)
                        ON DUPLICATE KEY UPDATE times_completed = times_completed + 1;
                        """.formatted(InitMariaDB.databaseName());
                fr.euphyllia.skyllia.sgbd.mariadb.execute.MariaDBExecute.executeQueryDML(
                        InitMariaDB.getPool(), q, List.of(islandId, id), null, null
                );
            } else {
                String q = """
                        INSERT INTO island_challenge_progress (island_id, challenge_id, times_completed)
                        VALUES(?, ?, 1)
                        ON CONFLICT(island_id, challenge_id) DO UPDATE SET times_completed = times_completed + 1;
                        """;
                InitSQLite.getPool().executeUpdate(q, List.of(islandId.toString(), id), null, null);
            }
        } catch (DatabaseException e) {
            log.error("An error occurred while incrementing challenge progress: {}", e.getMessage(), e);
        }
    }
}
