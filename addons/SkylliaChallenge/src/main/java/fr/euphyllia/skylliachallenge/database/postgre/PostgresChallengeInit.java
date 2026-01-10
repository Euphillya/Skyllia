package fr.euphyllia.skylliachallenge.database.postgre;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.postgre.Postgres;
import fr.euphyllia.skyllia.sgbd.postgre.PostgresLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;

public final class PostgresChallengeInit extends DatabaseInitializeQuery {

    private static DatabaseLoader pool;
    private static PostgresProgressBackend progressBackend;

    public PostgresChallengeInit() {
        Postgres pg = new Postgres(
                ConfigLoader.database.getPostgreConfig()
        );
        pool = new PostgresLoader(pg);
        progressBackend = new PostgresProgressBackend(pool);
    }

    public static DatabaseLoader getPool() {
        return pool;
    }

    public static PostgresProgressBackend getProgressBackend() {
        return progressBackend;
    }

    @Override
    public Boolean init() {
        try {
            SQLExecute.update(pool, """
                        CREATE TABLE IF NOT EXISTS island_challenge_progress(
                          island_id uuid NOT NULL,
                          challenge_id varchar(128) NOT NULL,
                          times_completed integer NOT NULL DEFAULT 0,
                          last_completed_at bigint NOT NULL DEFAULT 0,
                          PRIMARY KEY (island_id, challenge_id)
                        );
                    """, null);

            SQLExecute.update(pool, """
                        CREATE TABLE IF NOT EXISTS island_challenge_partial(
                          island_id uuid NOT NULL,
                          challenge_id varchar(128) NOT NULL,
                          requirement_id integer NOT NULL,
                          collected_amount bigint NOT NULL DEFAULT 0,
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
        } catch (Exception e) {
            return false;
        }
    }
}
