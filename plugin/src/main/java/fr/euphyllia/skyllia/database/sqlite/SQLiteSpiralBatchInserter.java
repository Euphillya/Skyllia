package fr.euphyllia.skyllia.database.sqlite;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.model.DBWork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles batch insertion of spiral data into the database (SQLite version).
 */
public class SQLiteSpiralBatchInserter implements DBWork {

    private static final Logger logger = LogManager.getLogger(SQLiteSpiralBatchInserter.class);
    // Vous pouvez adapter la taille du batch ou le supprimer.
    private static final int BATCH_SIZE = 1000;

    private final String insertQuery;
    private final List<IslandData> islands;

    /**
     * @param insertQuery La requête de type "INSERT OR IGNORE INTO spiral (id, region_x, region_z) VALUES (?, ?, ?)"
     * @param islands     Les données d’îles à insérer.
     */
    public SQLiteSpiralBatchInserter(String insertQuery, List<IslandData> islands) {
        this.insertQuery = insertQuery;
        this.islands = islands;
    }

    @Override
    public void run(Connection connection) throws DatabaseException {
        PreparedStatement preparedStatement = null;
        try {
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(insertQuery);

            int count = 0;
            for (IslandData island : islands) {
                preparedStatement.setInt(1, island.id());
                preparedStatement.setInt(2, island.regionX());
                preparedStatement.setInt(3, island.regionZ());
                preparedStatement.addBatch();

                if (++count % BATCH_SIZE == 0) {
                    preparedStatement.executeBatch();
                    connection.commit();
                }
            }
            preparedStatement.executeBatch();
            connection.commit();

        } catch (SQLException e) {
            logger.error("Error during batch insertion (SQLite)", e);
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error during rollback (SQLite)", rollbackEx);
            }
            throw new DatabaseException("Error during batch insertion (SQLite)", e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error("Error closing PreparedStatement (SQLite)", e);
                }
            }
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Error resetting auto-commit (SQLite)", e);
            }
        }
    }

    public record IslandData(int id, int regionX, int regionZ) {
    }
}
