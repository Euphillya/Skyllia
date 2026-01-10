package fr.euphyllia.skylliachallenge.database.mariadb;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDB;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDBLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;

public class MariaDBChallengeInit extends DatabaseInitializeQuery {

    private static final String CREATE_TABLE_PROGRESS = """
            CREATE TABLE IF NOT EXISTS `island_challenge_progress`(
              `island_id` CHAR(36) NOT NULL,
              `challenge_id` VARCHAR(128) NOT NULL,
              `times_completed` INT NOT NULL DEFAULT 0,
              `last_completed_at` BIGINT NOT NULL DEFAULT 0,
              PRIMARY KEY (`island_id`,`challenge_id`),
              INDEX `idx_progress_island` (`island_id`),
              INDEX `idx_progress_challenge` (`challenge_id`),
              INDEX `idx_progress_times` (`times_completed`),
              INDEX `idx_last_completed_at` (`last_completed_at`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;
    private static final String CREATE_TABLE_PARTIAL = """
            CREATE TABLE IF NOT EXISTS `island_challenge_partial`(
              `island_id` CHAR(36) NOT NULL,
              `challenge_id` VARCHAR(128) NOT NULL,
              `requirement_id` INT NOT NULL,
              `collected_amount` BIGINT NOT NULL DEFAULT 0,
              PRIMARY KEY (`island_id`,`challenge_id`,`requirement_id`),
              INDEX `idx_partial_island` (`island_id`),
              INDEX `idx_partial_challenge` (`challenge_id`),
              INDEX `idx_partial_requirement` (`requirement_id`),
              INDEX `idx_partial_collected` (`collected_amount`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """;
    private static DatabaseLoader database;
    private static MariaDBProgressBackend progressBackend;

    public MariaDBChallengeInit() {
        MariaDB mariaDB = new MariaDB(ConfigLoader.database.getMariaDBConfig());
        database = new MariaDBLoader(mariaDB);
        progressBackend = new MariaDBProgressBackend(database);
    }

    public static MariaDBProgressBackend getProgressBackend() {
        return progressBackend;
    }

    public static DatabaseLoader getPool() {
        return database;
    }

    @Override
    public Boolean init() {
        try {
            if (!database.loadDatabase()) {
                return false;
            }
            SQLExecute.update(database, CREATE_TABLE_PROGRESS, null);
            SQLExecute.update(database, CREATE_TABLE_PARTIAL, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
