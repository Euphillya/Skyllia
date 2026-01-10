package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.skyblock.IslandData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles batch insertion of spiral data into the database.
 */
public class SpiralBatchInserter {

    private static final Logger logger = LogManager.getLogger(SpiralBatchInserter.class);
    private static final int BATCH_SIZE = 1000;

    private final String insertQuery;
    private final List<IslandData> islands;

    public SpiralBatchInserter(String insertQuery, List<IslandData> islands) {
        this.insertQuery = insertQuery;
        this.islands = islands;
    }

    public void run(Connection connection) throws SQLException {
        boolean oldAutoCommit = connection.getAutoCommit();
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {

            connection.setAutoCommit(false);

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
            logger.error("Error during batch insertion", e);
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error during rollback", rollbackEx);
                e.addSuppressed(rollbackEx);
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(oldAutoCommit);
            } catch (SQLException e) {
                logger.error("Error restoring auto-commit", e);
            }
        }
    }
}
