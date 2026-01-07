package fr.euphyllia.skylliachallenge.storage;

import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDB;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDBLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.MariaDBExecute;

public final class InitMariaDB {
    private static MariaDBLoader pool;
    private static String dbName;

    private InitMariaDB() {
    }

    public static boolean initIfConfigured() throws DatabaseException {
        if (ConfigLoader.database.getMariaDBConfig() == null) return false;
        MariaDB maria = new MariaDB(ConfigLoader.database.getMariaDBConfig());
        pool = new MariaDBLoader(maria);
        dbName = ConfigLoader.database.getMariaDBConfig().database();
        if (!pool.loadDatabase()) return false;

        // Table
        String create = """
                    CREATE TABLE IF NOT EXISTS `%s`.`island_challenge_progress`(
                      `island_id` CHAR(36) NOT NULL,
                      `challenge_id` VARCHAR(128) NOT NULL,
                      `times_completed` INT NOT NULL DEFAULT 0,
                      PRIMARY KEY (`island_id`,`challenge_id`),
                      INDEX `idx_island_only` (`island_id`),
                      INDEX `idx_challenge_only` (`challenge_id`),
                      INDEX `idx_times_completed` (`times_completed`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """.formatted(dbName);

        String createPartial = """
                    CREATE TABLE IF NOT EXISTS `%s`.`island_challenge_partial`(
                      `island_id` CHAR(36) NOT NULL,
                      `challenge_id` VARCHAR(128) NOT NULL,
                      `requirement_id` INT NOT NULL,
                      `collected_amount` BIGINT NOT NULL DEFAULT 0,
                      PRIMARY KEY (`island_id`,`challenge_id`,`requirement_id`),
                      INDEX `idx_island_only` (`island_id`),
                      INDEX `idx_challenge_only` (`challenge_id`),
                      INDEX `idx_requirement_only` (`requirement_id`),
                      INDEX `idx_collected_amount` (`collected_amount`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """.formatted(dbName);

        String alterLastCompleted = """
                    ALTER TABLE `%s`.`island_challenge_progress`
                    ADD COLUMN IF NOT EXISTS `last_completed_at` BIGINT NOT NULL DEFAULT 0;
                """.formatted(dbName);

        String alterIndex = """
                    CREATE INDEX IF NOT EXISTS `idx_last_completed_at` 
                    ON `%s`.`island_challenge_progress` (`last_completed_at`);
                """.formatted(dbName);

        MariaDBExecute.executeQuery(pool, create);
        MariaDBExecute.executeQuery(pool, createPartial);
        MariaDBExecute.executeQuery(pool, alterLastCompleted);
        MariaDBExecute.executeQuery(pool, alterIndex);

        return true;
    }

    public static MariaDBLoader getPool() {
        return pool;
    }

    public static String databaseName() {
        return dbName;
    }
}