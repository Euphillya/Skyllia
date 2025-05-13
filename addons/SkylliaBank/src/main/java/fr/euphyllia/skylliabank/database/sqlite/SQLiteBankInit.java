package fr.euphyllia.skylliabank.database.sqlite;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLite;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;

public class SQLiteBankInit extends DatabaseInitializeQuery {

    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS island_bank (
                island_id TEXT PRIMARY KEY,
                balance REAL NOT NULL DEFAULT 0
            );
            """;
    private static SQLiteDatabaseLoader database;
    private static SQLiteBankGenerator sqliteBankGenerator;

    public SQLiteBankInit() {
        SQLite sqlite = new SQLite(ConfigLoader.database.getSqLiteConfig());
        database = new SQLiteDatabaseLoader(sqlite);
        sqliteBankGenerator = new SQLiteBankGenerator();
    }

    public static SQLiteDatabaseLoader getPool() {
        return database;
    }

    public static SQLiteBankGenerator getGenerator() {
        return sqliteBankGenerator;
    }

    @Override
    public boolean init() throws DatabaseException {
        if (!database.loadDatabase()) {
            return false;
        }
        database.executeUpdate(CREATE_TABLE, null, null, null);
        return true;
    }
}