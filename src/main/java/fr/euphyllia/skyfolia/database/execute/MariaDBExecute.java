package fr.euphyllia.skyfolia.database.execute;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.database.DatabaseLoader;
import fr.euphyllia.skyfolia.database.model.DBCallback;
import fr.euphyllia.skyfolia.database.model.DBCallbackInt;
import fr.euphyllia.skyfolia.database.model.DBWork;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MariaDBExecute {

    private static final String DATABASE_NOT_FOUND_ERROR = "Cannot get connection to the database";
    private static final Logger logger = LogManager.getLogger("fr.euphyllia.skyfolia.database.execute.MariaDBExecute");

    public static void executeQuery(InterneAPI internalApi, String query) throws SQLException {
        executeQuery(internalApi, query, null, null, null);
    }

    public static void executeQuery(InterneAPI internalApi, String query, List<?> param, DBCallback callback, DBWork work) {
        DatabaseLoader pool = internalApi.getDatabaseLoader();
        if (pool == null) {
            throw new NullPointerException(DATABASE_NOT_FOUND_ERROR);
        }
        Connection connection = pool.getMariaDBConnection();
        if (connection == null) {
            throw new NullPointerException(DATABASE_NOT_FOUND_ERROR);
        }
        if (work != null) {
            work.run(connection);
            return;
        }

        try {
            ResultSet resultSet = pool.execute(connection, query, param);
            if (callback != null) {
                callback.run(resultSet);
            }
            connection.close();
        } catch (SQLException exception) {
            logger.log(Level.FATAL, "[MARIADB] - Query : %s".formatted(query));
            throw new RuntimeException(exception);
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
    public static void executeQueryDML(InterneAPI internalApi, String query, List<?> param, DBCallbackInt callback, DBWork work) {
        DatabaseLoader pool = internalApi.getDatabaseLoader();
        if (pool == null) {
            throw new NullPointerException(DATABASE_NOT_FOUND_ERROR);
        }
        Connection connection = pool.getMariaDBConnection();
        if (connection == null) {
            throw new NullPointerException(DATABASE_NOT_FOUND_ERROR);
        } else if (work != null) {
            work.run(connection);
        } else {
            try {
                int resultInt = pool.executeInt(connection, query, param);
                if (callback != null) {
                    callback.run(resultInt);
                }

                connection.close();
            } catch (SQLException exception) {
                logger.log(Level.FATAL, "[MARIADB] - Query : %s".formatted(query));
                throw new RuntimeException(exception);
            }
        }
    }
}
