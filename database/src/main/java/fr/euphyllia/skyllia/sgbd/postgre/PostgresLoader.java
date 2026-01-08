package fr.euphyllia.skyllia.sgbd.postgre;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.stream.AsciiStream;
import fr.euphyllia.skyllia.sgbd.utils.stream.BinaryStream;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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