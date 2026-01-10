package fr.euphyllia.skylliachallenge.database.sqlite;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLite;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;

public final class SQLiteChallengeInit extends DatabaseInitializeQuery {

    private static DatabaseLoader pool;
    private static SQLiteProgressBackend progressBackend;

    public SQLiteChallengeInit() {
        SQLite sqlite = new SQLite(ConfigLoader.database.getSqLiteConfig());
        pool = new SQLiteDatabaseLoader(sqlite);
        progressBackend = new SQLiteProgressBackend(pool);
    }

    public static DatabaseLoader getPool() {
        return pool;
    }

    public static SQLiteProgressBackend getProgressBackend() {
        return progressBackend;
    }

    @Override
    public Boolean init() {
        try {
            if (!pool.loadDatabase()) return false;

            SQLExecute.update(pool, """
                        CREATE TABLE IF NOT EXISTS island_challenge_progress(
                          island_id TEXT NOT NULL,
                          challenge_id TEXT NOT NULL,
                          times_completed INTEGER NOT NULL DEFAULT 0,
                          last_completed_at INTEGER NOT NULL DEFAULT 0,
                          PRIMARY KEY (island_id, challenge_id)
                        );
                    """, null);

            SQLExecute.update(pool, """
                        CREATE TABLE IF NOT EXISTS island_challenge_partial(
                          island_id TEXT NOT NULL,
                          challenge_id TEXT NOT NULL,
                          requirement_id INTEGER NOT NULL,
                          collected_amount INTEGER NOT NULL DEFAULT 0,
                          PRIMARY KEY (island_id, challenge_id, requirement_id)
                        );
                    """, null);

            SQLExecute.update(pool, "CREATE INDEX IF NOT EXISTS idx_progress_island ON island_challenge_progress(island_id);", null);
            SQLExecute.update(pool, "CREATE INDEX IF NOT EXISTS idx_progress_challenge ON island_challenge_progress(challenge_id);", null);
            SQLExecute.update(pool, "CREATE INDEX IF NOT EXISTS idx_progress_times ON island_challenge_progress(times_completed);", null);
            SQLExecute.update(pool, "CREATE INDEX IF NOT EXISTS idx_last_completed_at ON island_challenge_progress(last_completed_at);", null);

            SQLExecute.update(pool, "CREATE INDEX IF NOT EXISTS idx_partial_island ON island_challenge_partial(island_id);", null);
            SQLExecute.update(pool, "CREATE INDEX IF NOT EXISTS idx_partial_challenge ON island_challenge_partial(challenge_id);", null);
            SQLExecute.update(pool, "CREATE INDEX IF NOT EXISTS idx_partial_requirement ON island_challenge_partial(requirement_id);", null);
            SQLExecute.update(pool, "CREATE INDEX IF NOT EXISTS idx_partial_collected ON island_challenge_partial(collected_amount);", null);

            return true;
        } catch (DatabaseException e) {
            return false;
        }
    }
}
