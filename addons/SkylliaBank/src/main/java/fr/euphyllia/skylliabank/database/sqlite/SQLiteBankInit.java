package fr.euphyllia.skylliabank.database.sqlite;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLite;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;

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
        sqliteBankGenerator = new SQLiteBankGenerator(database);
    }

    public static SQLiteDatabaseLoader getPool() {
        return database;
    }

    public static SQLiteBankGenerator getGenerator() {
        return sqliteBankGenerator;
    }

    @Override
    public Boolean init() {
        try {
            if (!database.loadDatabase()) {
                return false;
            }
            SQLExecute.update(database, CREATE_TABLE, null);
            return true;
        } catch (DatabaseException e) {
            return false;
        }
    }
}