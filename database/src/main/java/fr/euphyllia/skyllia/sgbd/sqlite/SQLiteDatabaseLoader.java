package fr.euphyllia.skyllia.sgbd.sqlite;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabaseLoader implements DatabaseLoader {

    private final SQLite sqlite;

    public SQLiteDatabaseLoader(SQLite sqlite) {
        this.sqlite = sqlite;
    }

    @Override
    public boolean loadDatabase() throws DatabaseException {
        boolean connected = sqlite.onLoad();
        if (connected) enableWAL();
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

    private void enableWAL() throws DatabaseException {
        try (Connection c = getConnection();
             Statement st = c.createStatement()) {

            st.execute("PRAGMA journal_mode = WAL;");
            st.execute("PRAGMA synchronous = NORMAL;");
            st.execute("PRAGMA busy_timeout = 30000;");
            st.execute("PRAGMA foreign_keys = ON;");

        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
