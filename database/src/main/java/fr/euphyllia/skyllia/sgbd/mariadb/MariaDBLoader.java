package fr.euphyllia.skyllia.sgbd.mariadb;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;

import java.sql.Connection;

/**
 * The {@code DatabaseLoader} class provides methods to load and close a MariaDB connection pool,
 * execute SQL queries, and handle prepared statements with various parameter types.
 */
public record MariaDBLoader(MariaDB mariaDB) implements DatabaseLoader {

    @Override
    public boolean loadDatabase() throws DatabaseException {
        return mariaDB != null && !mariaDB.isConnected() && mariaDB.onLoad();
    }

    @Override
    public void closeDatabase() {
        if (mariaDB != null) mariaDB.onClose();
    }

    @Override
    public Connection getConnection() throws DatabaseException {
        return mariaDB != null ? mariaDB.getConnection() : null;
    }
}
