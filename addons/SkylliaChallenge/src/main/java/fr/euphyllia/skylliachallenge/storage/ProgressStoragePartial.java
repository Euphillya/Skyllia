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
import java.util.concurrent.atomic.AtomicLong;

public class ProgressStoragePartial {

    private static final Logger log = LoggerFactory.getLogger(ProgressStoragePartial.class);

    private ProgressStoragePartial() {
    }

    static boolean useMaria() {
        return InitMariaDB.getPool() != null;
    }

    public static long getPartial(UUID islandId, NamespacedKey challengeId, int requirementId) {
        String cid = challengeId.asString();
        AtomicLong res = new AtomicLong(0L);
        try {
            if (useMaria()) {
                String q = """
                        SELECT collected_amount
                        FROM `%s`.`island_challenge_partial`
                        WHERE island_id=? AND challenge_id=? AND requirement_id=?;
                        """.formatted(InitMariaDB.databaseName());
                MariaDBExecute.executeQuery(InitMariaDB.getPool(), q, List.of(islandId, cid, requirementId), rs -> {
                    try {
                        if (rs != null && rs.next()) res.set(rs.getLong("collected_amount"));
                    } catch (
                            SQLException ignored) {
                    }
                }, null);
            } else {
                String q = """
                        SELECT collected_amount
                        FROM island_challenge_partial
                        WHERE island_id=? AND challenge_id=? AND requirement_id=?;
                        """;
                SQLiteDatabaseLoader db = InitSQLite.getPool();
                db.executeQuery(q, List.of(islandId.toString(), cid, requirementId), rs -> {
                    try {
                        if (rs != null && rs.next()) res.set(rs.getLong("collected_amount"));
                    } catch (SQLException ignored) {
                    }
                }, null);
            }
        } catch (DatabaseException e) {
            log.error("getPartial error: {}", e.getMessage(), e);
        }
        return res.get();
    }

    public static void addPartial(UUID islandId, NamespacedKey challengeId, int requirementId, long delta) {
        if (delta <= 0) return;
        String cid = challengeId.asString();
        try {
            if (useMaria()) {
                String q = """
                        INSERT INTO `%s`.`island_challenge_partial` (island_id, challenge_id, requirement_id, collected_amount)
                        VALUES(?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE collected_amount = collected_amount + VALUES(collected_amount);
                        """.formatted(InitMariaDB.databaseName());
                MariaDBExecute.executeQueryDML(InitMariaDB.getPool(), q, List.of(islandId, cid, requirementId, delta), null, null);
            } else {
                String q = """
                        INSERT INTO island_challenge_partial (island_id, challenge_id, requirement_id, collected_amount)
                        VALUES(?, ?, ?, ?)
                        ON CONFLICT(island_id, challenge_id, requirement_id)
                        DO UPDATE SET collected_amount = collected_amount + excluded.collected_amount;
                        """;
                InitSQLite.getPool().executeUpdate(q, List.of(islandId.toString(), cid, requirementId, delta), null, null);
            }
        } catch (DatabaseException e) {
            log.error("addPartial error: {}", e.getMessage(), e);
        }
    }
}
