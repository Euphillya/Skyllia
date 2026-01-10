package fr.euphyllia.skyllia.sgbd.postgre;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;

import java.sql.Connection;

public record PostgresLoader(Postgres pg) implements DatabaseLoader {

    @Override
    public boolean loadDatabase() throws DatabaseException {
        return pg != null && !pg.isConnected() && pg.onLoad();
    }

    @Override
    public void closeDatabase() {
        if (pg != null) pg.onClose();
    }

    @Override
    public Connection getConnection() throws DatabaseException {
        return pg != null ? pg.getConnection() : null;
    }
}