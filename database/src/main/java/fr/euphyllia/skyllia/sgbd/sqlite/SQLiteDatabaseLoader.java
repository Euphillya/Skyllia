package fr.euphyllia.skyllia.sgbd.sqlite;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.model.DBCallback;
import fr.euphyllia.skyllia.sgbd.model.DBCallbackInt;
import fr.euphyllia.skyllia.sgbd.model.DBWork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SQLiteDatabaseLoader {

    private final SQLite sqlite;
    private final Logger logger = LogManager.getLogger(SQLiteDatabaseLoader.class);

    public SQLiteDatabaseLoader(SQLite sqlite) {
        this.sqlite = sqlite;
    }

    public boolean loadDatabase() throws DatabaseException {
        return sqlite.onLoad();
    }

    public void closeDatabase() {
        sqlite.onClose();
    }

    public Connection getSQLiteConnection() throws DatabaseException {
        return sqlite.getConnection();
    }

    public void executeQuery(String query, List<?> params, DBCallback callback, DBWork work) throws DatabaseException {
        try (Connection connection = getSQLiteConnection()) {
            if (work != null) {
                work.run(connection);
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                setParams(stmt, params);
                ResultSet rs = stmt.executeQuery();
                if (callback != null) {
                    callback.run(rs);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void executeQueryDML(String query, List<?> params, DBCallbackInt callback, DBWork work) throws DatabaseException {
        try (Connection connection = getSQLiteConnection()) {
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
}