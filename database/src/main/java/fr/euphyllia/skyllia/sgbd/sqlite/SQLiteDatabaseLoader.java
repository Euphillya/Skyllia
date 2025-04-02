package fr.euphyllia.skyllia.sgbd.sqlite;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.model.DBCallback;
import fr.euphyllia.skyllia.sgbd.model.DBCallbackInt;
import fr.euphyllia.skyllia.sgbd.model.DBWork;
import fr.euphyllia.skyllia.sgbd.model.DatabaseLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.List;

public class SQLiteDatabaseLoader implements DatabaseLoader {

    private final SQLite sqlite;
    private final Logger logger = LogManager.getLogger(SQLiteDatabaseLoader.class);

    public SQLiteDatabaseLoader(SQLite sqlite) {
        this.sqlite = sqlite;
    }

    @Override
    public boolean loadDatabase() throws DatabaseException {
        boolean connected = sqlite.onLoad();
        if (connected) {
            enableWAL();
        }
        return connected;
    }

    @Override
    public void closeDatabase() {
        sqlite.onClose();
    }

    @Override
    public Connection getConnection() throws DatabaseException {
        return sqlite.getConnection();
    }

    /**
     * Pour les requêtes SELECT (renvoient un ResultSet).
     * Appeler ceci sur un CREATE/INSERT/UPDATE/DELETE fera une SQLException "Query does not return results".
     */
    public void executeQuery(String query, List<?> params, DBCallback callback, DBWork work) throws DatabaseException {
        try (Connection connection = getConnection()) {
            if (work != null) {
                work.run(connection);
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                setParams(stmt, params);
                // Ici, c'est un SELECT => rs
                try (ResultSet rs = stmt.executeQuery()) {
                    if (callback != null) {
                        callback.run(rs);
                    }
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Pour les requêtes qui ne renvoient pas de ResultSet (CREATE TABLE, INSERT, UPDATE, DELETE...).
     * On récupère le nombre de lignes affectées (ou 0 pour un CREATE TABLE).
     */
    public void executeUpdate(String query, List<?> params, DBCallbackInt callback, DBWork work) throws DatabaseException {
        try (Connection connection = getConnection()) {
            if (work != null) {
                work.run(connection);
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                setParams(stmt, params);
                int affectedRows = stmt.executeUpdate();
                if (callback != null) {
                    callback.run(affectedRows);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void setParams(PreparedStatement stmt, List<?> params) throws SQLException {
        if (params != null) {
            int i = 1;
            for (Object param : params) {
                stmt.setObject(i++, param);
            }
        }
    }

    public void enableWAL() throws DatabaseException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA journal_mode = WAL;");
            statement.execute("PRAGMA synchronous = NORMAL;");
            statement.execute("PRAGMA busy_timeout = 30000;");
            statement.execute("PRAGMA foreign_keys = ON;");

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
