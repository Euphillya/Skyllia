package fr.euphyllia.skyllia.database.postgresql;

import fr.euphyllia.skyllia.api.skyblock.IslandData;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.model.DBWork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PostgreSQLSpiralBatchInserter implements DBWork {

    private static final Logger logger = LogManager.getLogger(PostgreSQLSpiralBatchInserter.class);
    private static final int BATCH_SIZE = 1000;

    private final String insertQuery;
    private final List<IslandData> islands;

    public PostgreSQLSpiralBatchInserter(String insertQuery, List<IslandData> islands) {
        this.insertQuery = insertQuery;
        this.islands = islands;
    }

    @Override
    public void run(Connection connection) throws DatabaseException {
        PreparedStatement ps = null;
        try {
            connection.setAutoCommit(false);
            ps = connection.prepareStatement(insertQuery);

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
            logger.error("Error during batch insertion (PostgreSQL)", e);
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error during rollback (PostgreSQL)", rollbackEx);
            }
            throw new DatabaseException("Error during batch insertion (PostgreSQL)", e);
        } finally {
            if (ps != null) {
                try { ps.close(); } catch (SQLException e) { logger.error("Error closing PreparedStatement", e); }
            }
            try { connection.setAutoCommit(true); } catch (SQLException e) { logger.error("Error resetting auto-commit", e); }
        }
    }
}
