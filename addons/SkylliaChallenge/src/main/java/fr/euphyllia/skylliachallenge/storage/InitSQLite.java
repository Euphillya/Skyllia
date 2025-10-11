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

        String createPartial = """
                    CREATE TABLE IF NOT EXISTS island_challenge_partial(
                      island_id TEXT NOT NULL,
                      challenge_id TEXT NOT NULL,
                      requirement_id INTEGER NOT NULL,
                      collected_amount INTEGER NOT NULL DEFAULT 0,
                      PRIMARY KEY (island_id, challenge_id, requirement_id)
                    );
                """;

        String index1 = "CREATE INDEX IF NOT EXISTS idx_progress_island ON island_challenge_progress(island_id);";
        String index2 = "CREATE INDEX IF NOT EXISTS idx_progress_challenge ON island_challenge_progress(challenge_id);";
        String index3 = "CREATE INDEX IF NOT EXISTS idx_progress_times ON island_challenge_progress(times_completed);";

        String pindex1 = "CREATE INDEX IF NOT EXISTS idx_partial_island ON island_challenge_partial(island_id);";
        String pindex2 = "CREATE INDEX IF NOT EXISTS idx_partial_challenge ON island_challenge_partial(challenge_id);";
        String pindex3 = "CREATE INDEX IF NOT EXISTS idx_partial_requirement ON island_challenge_partial(requirement_id);";
        String pindex4 = "CREATE INDEX IF NOT EXISTS idx_partial_collected ON island_challenge_partial(collected_amount);";

        pool.executeUpdate(create, null, null, null);
        pool.executeUpdate(createPartial, null, null, null);

        pool.executeUpdate(index1, null, null, null);
        pool.executeUpdate(index2, null, null, null);
        pool.executeUpdate(index3, null, null, null);

        pool.executeUpdate(pindex1, null, null, null);
        pool.executeUpdate(pindex2, null, null, null);
        pool.executeUpdate(pindex3, null, null, null);
        pool.executeUpdate(pindex4, null, null, null);

        return true;
    }

    public static SQLiteDatabaseLoader getPool() {
        return pool;
    }
}
