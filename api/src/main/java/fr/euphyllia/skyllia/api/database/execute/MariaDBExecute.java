package fr.euphyllia.skyllia.api.database.execute;

import fr.euphyllia.skyllia.api.database.DatabaseLoader;
import fr.euphyllia.skyllia.api.database.model.DBCallback;
import fr.euphyllia.skyllia.api.database.model.DBCallbackInt;
import fr.euphyllia.skyllia.api.database.model.DBWork;
import fr.euphyllia.skyllia.api.exceptions.DatabaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MariaDBExecute {

    private static final String DATABASE_NOT_FOUND_ERROR = "Cannot get connection to the database";
    private static final Logger logger = LogManager.getLogger(MariaDBExecute.class);

    public static void executeQuery(DatabaseLoader pool, String query) {
        try {
            executeQuery(pool, query, null, null, null, false);
        } catch (DatabaseException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
    }

    public static void executeQuery(DatabaseLoader pool, String query, List<?> param, DBCallback callback, DBWork work) {
        try {
            executeQuery(pool, query, param, callback, work, false);
        } catch (DatabaseException e) {
            logger.log(Level.FATAL, e.getMessage(), e);
        }
    }

    public static void executeQuery(DatabaseLoader pool, String query, List<?> param, DBCallback callback, DBWork work, boolean ignoreError) throws DatabaseException {
        if (pool == null) {
            throw new DatabaseException(DATABASE_NOT_FOUND_ERROR);
        }
        try {
            Connection connection = pool.getMariaDBConnection();
            if (connection == null) {
                throw new DatabaseException(DATABASE_NOT_FOUND_ERROR);
            }
            if (work != null) {
                work.run(connection);
                return;
            }
            ResultSet resultSet = pool.execute(connection, query, param);
            if (callback != null) {
                callback.run(resultSet);
            }
            connection.close();
        } catch (SQLException | DatabaseException exception) {
            if (Boolean.FALSE.equals(ignoreError)) {
                logger.log(Level.FATAL, "[MARIADB] - Query : %s".formatted(query), exception);
                throw new DatabaseException(exception);
            }
        }
    }

    /**
     * Le SQL Data Manipulation Langage (DML) est utilisé lorsqu'on utilise "UPDATE / INSERT / DELETE"
     *
     * @param query    Requête SQL
     * @param param    Liste des valeurs
     * @param callback rendu
     * @param work     pool connexion
     */
    public static void executeQueryDML(DatabaseLoader pool, String query, List<?> param, DBCallbackInt callback, DBWork work) {
        if (pool == null) {
            throw new NullPointerException(DATABASE_NOT_FOUND_ERROR);
        }
        try {
            Connection connection = pool.getMariaDBConnection();
            if (connection == null) {
                throw new NullPointerException(DATABASE_NOT_FOUND_ERROR);
            } else if (work != null) {
                work.run(connection);
            } else {
                int resultInt = pool.executeInt(connection, query, param);
                if (callback != null) {
                    callback.run(resultInt);
                }

                connection.close();
            }
        } catch (SQLException | DatabaseException exception) {
            logger.log(Level.FATAL, exception.getMessage(), exception);
        }
    }
}
