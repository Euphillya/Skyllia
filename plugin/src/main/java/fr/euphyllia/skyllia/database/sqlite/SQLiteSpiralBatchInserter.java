package fr.euphyllia.skyllia.database.sqlite;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles batch insertion of spiral data into the database (SQLite version).
 */
public class SQLiteSpiralBatchInserter {

    private static final Logger logger = LogManager.getLogger(SQLiteSpiralBatchInserter.class);
    private static final int BATCH_SIZE = 1000;

    private final String insertQuery;
    private final List<IslandData> islands;

    /**
     * @param insertQuery Query like: "INSERT OR IGNORE INTO spiral (id, region_x, region_z) VALUES (?, ?, ?)"
     * @param islands     List of island data to insert.
     */
    public SQLiteSpiralBatchInserter(String insertQuery, List<IslandData> islands) {
        this.insertQuery = insertQuery;
        this.islands = islands;
    }

    public void run(Connection connection) throws SQLException {
        boolean oldAutoCommit = connection.getAutoCommit();

        try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
            connection.setAutoCommit(false);

            int count = 0;
            for (IslandData island : islands) {
                ps.setInt(1, island.id());
                ps.setInt(2, island.regionX());
                ps.setInt(3, island.regionZ());
                ps.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    connection.commit();
                }
            }

            ps.executeBatch();
            connection.commit();

        } catch (SQLException e) {
            logger.error("Error during batch insertion (SQLite)", e);
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error during rollback (SQLite)", rollbackEx);
                e.addSuppressed(rollbackEx);
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                logger.error("Error restoring auto-commit (SQLite)", e);
            }
        }
    }

    public record IslandData(int id, int regionX, int regionZ) {
    }
}
