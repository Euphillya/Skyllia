package fr.euphyllia.skylliachallenge.storage;

import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLite;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;

public final class InitSQLite {
    private static SQLiteDatabaseLoader pool;

    private InitSQLite() {
    }

    public static boolean initIfConfigured() throws DatabaseException {
        if (ConfigLoader.database.getSqLiteConfig() == null) return false;
        SQLite sqlite = new SQLite(ConfigLoader.database.getSqLiteConfig());
        pool = new SQLiteDatabaseLoader(sqlite);
        if (!pool.loadDatabase()) return false;

        String create = """
                CREATE TABLE IF NOT EXISTS island_challenge_progress(
                  island_id TEXT NOT NULL,
                  challenge_id TEXT NOT NULL,
                  times_completed INTEGER NOT NULL DEFAULT 0,
                  PRIMARY KEY (island_id, challenge_id)
                );
                """;
        pool.executeUpdate(create, null, null, null);
        return true;
    }

    public static SQLiteDatabaseLoader getPool() {
        return pool;
    }
}
