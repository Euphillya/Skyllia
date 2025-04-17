package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.skyblock.IslandData;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.model.DBWork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles batch insertion of spiral data into the database.
 */
public class SpiralBatchInserter implements DBWork {

    private static final Logger logger = LogManager.getLogger(SpiralBatchInserter.class);
    private static final int BATCH_SIZE = 1000;
    private final String insertQuery;
    private final List<IslandData> islands;

    /**
     * Constructs a SpiralBatchInserter.
     *
     * @param insertQuery The formatted SQL insert query.
     * @param islands     The list of island data to insert.
     */
    public SpiralBatchInserter(String insertQuery, List<IslandData> islands) {
        this.insertQuery = insertQuery;
        this.islands = islands;
    }

    @Override
    public void run(Connection connection) throws DatabaseException {
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(insertQuery);
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
            }
            throw new DatabaseException("Error during batch insertion", e);
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    logger.error("Error closing PreparedStatement", e);
                }
            }
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Error resetting auto-commit", e);
            }
        }
    }
}