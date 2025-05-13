package fr.euphyllia.skyllia.sgbd.mariadb.execute;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDBLoader;
import fr.euphyllia.skyllia.sgbd.model.DBCallback;
import fr.euphyllia.skyllia.sgbd.model.DBCallbackInt;
import fr.euphyllia.skyllia.sgbd.model.DBWork;
import fr.euphyllia.skyllia.sgbd.model.DatabaseLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Utility class for executing MariaDB queries with optional callbacks and custom work.
 */
public class MariaDBExecute {

    private static final String DATABASE_NOT_FOUND_ERROR = "Cannot get connection to the database";
    private static final Logger logger = LogManager.getLogger(MariaDBExecute.class);

    /**
     * Executes a SQL query without parameters, without callback, and without custom work.
     *
     * @param pool  The connection pool to use.
     * @param query The SQL query to execute.
     * @throws DatabaseException If an error occurs during query execution.
     */
    public static void executeQuery(DatabaseLoader pool, String query) throws DatabaseException {
        executeQuery(pool, query, null, null, null);
    }

    /**
     * Executes a SQL query with optional parameters, an optional callback, and optional custom work.
     *
     * @param databaseLoader The connection pool to use.
     * @param query          The SQL query to execute.
     * @param param          The list of parameters for the query.
     * @param callback       The callback to execute with the query result.
     * @param work           The work to execute with the connection.
     *                       <p>
     *                       <strong>Important:</strong> If `work` is not null, the user is responsible for closing the connection.
     *                       </p>
     * @throws DatabaseException If an error occurs during query execution.
     * @implSpec <ul>
     * <li>If `work` is provided, executes `work.run(connection)` and does not close the connection. The user must close the connection.</li>
     * <li>Otherwise, executes the query, invokes the callback if provided, and closes the connection before executing the callback.</li>
     * </ul>
     */
    public static void executeQuery(DatabaseLoader databaseLoader, String query, List<?> param, DBCallback callback, DBWork work) throws DatabaseException {
        if (databaseLoader == null) {
            throw new DatabaseException(DATABASE_NOT_FOUND_ERROR);
        }
        MariaDBLoader pool = (MariaDBLoader) databaseLoader;
        Connection connection = null;
        try {
            connection = pool.getConnection();
            if (connection == null) {
                throw new DatabaseException(DATABASE_NOT_FOUND_ERROR);
            }
            if (work != null) {
                work.run(connection);
                return;
            }
            ResultSet resultSet = pool.execute(connection, query, param);
            connection.close();
            if (callback != null) {
                callback.run(resultSet);
            }
        } catch (SQLException | DatabaseException exception) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    logger.error("Erreur lors de la fermeture de la connexion après une exception", closeException);
                    throw new DatabaseException(closeException);
                }
            }
            throw new DatabaseException(exception);
        }
    }

    /**
     * Executes a DML SQL query (UPDATE, INSERT, DELETE) with optional parameters, an optional callback, and optional custom work.
     *
     * @param databaseLoader The connection pool to use.
     * @param query          The DML SQL query to execute.
     * @param param          The list of parameters for the query.
     * @param callback       The callback to execute with the query result (number of affected rows).
     * @param work           The work to execute with the connection.
     *                       <p>
     *                       <strong>Important:</strong> If `work` is not null, the user is responsible for closing the connection.
     *                       </p>
     * @throws DatabaseException If an error occurs during query execution.
     * @implSpec <ul>
     * <li>If `work` is provided, executes `work.run(connection)` and does not close the connection. The user must close the connection.</li>
     * <li>Otherwise, executes the DML query, invokes the callback if provided, and closes the connection before executing the callback.</li>
     * </ul>
     */
    public static void executeQueryDML(DatabaseLoader databaseLoader, String query, List<?> param, DBCallbackInt callback, DBWork work) throws DatabaseException {
        if (databaseLoader == null) {
            throw new DatabaseException(DATABASE_NOT_FOUND_ERROR);
        }
        MariaDBLoader pool = (MariaDBLoader) databaseLoader;
        Connection connection = null;
        try {
            connection = pool.getConnection();
            if (connection == null) {
                throw new DatabaseException(DATABASE_NOT_FOUND_ERROR);
            }

            if (work != null) {
                work.run(connection);
                return;
            }

            int resultInt = pool.executeInt(connection, query, param);
            connection.close();
            if (callback != null) {
                callback.run(resultInt);
            }
        } catch (SQLException | DatabaseException exception) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    logger.error("Erreur lors de la fermeture de la connexion après une exception", closeException);
                    throw new DatabaseException(closeException);
                }
            }
            throw new DatabaseException(exception);
        }
    }
}
