package fr.euphyllia.skyllia.database.query;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.exceptions.DatabaseException;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.section.MariaDBConfig;
import fr.euphyllia.skyllia.database.execute.MariaDBExecute;
import fr.euphyllia.skyllia.utils.RegionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MariaDBCreateTable {

    private static final String CREATE_DATABASE = """
            CREATE DATABASE IF NOT EXISTS `%s`;
            """;
    private static final String CREATE_ISLANDS = """
            CREATE TABLE IF NOT EXISTS `%s`.`islands` (
            `island_type` CHAR(36) NOT NULL,
            `island_id` CHAR(36) NOT NULL,
            `uuid_owner` CHAR(36) NOT NULL,
            `disable` TINYINT DEFAULT '0',
            `region_x` INT NOT NULL,
            `region_z` INT NOT NULL,
            `private` TINYINT DEFAULT '0',
            `size` DOUBLE NOT NULL,
            `create_time` TIMESTAMP,
            PRIMARY KEY (`island_id`, `uuid_owner`, `region_x`, `region_z`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String CREATE_ISLANDS_MEMBERS = """
                CREATE TABLE IF NOT EXISTS `%s`.`members_in_islands` (
                  `island_id` CHAR(36) NOT NULL,
                  `uuid_player` CHAR(36) NOT NULL,
                  `player_name` VARCHAR(40) DEFAULT NULL,
                  `role` VARCHAR(40) DEFAULT NULL,
                  `joined` TIMESTAMP,
                  PRIMARY KEY (`island_id`,`uuid_player`),
                  CONSTRAINT `members_in_islands_FK` FOREIGN KEY (`island_id`) REFERENCES `islands` (`island_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String CREATE_ISLANDS_WARP = """
                CREATE TABLE IF NOT EXISTS `%s`.`islands_warp` (
                  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
                  `island_id` CHAR(36) NOT NULL,
                  `warp_name` VARCHAR(100) DEFAULT NULL,
                  `world_name` VARCHAR(100) DEFAULT NULL,
                  `x` DOUBLE DEFAULT NULL,
                  `y` DOUBLE DEFAULT NULL,
                  `z` DOUBLE DEFAULT NULL,
                  `pitch` FLOAT DEFAULT NULL,
                  `yaw` FLOAT DEFAULT NULL,
                  PRIMARY KEY (`id`),
                  KEY `islands_warp_FK` (`island_id`),
                  UNIQUE KEY `unique_warp_per_island` (`island_id`, `warp_name`),
                  CONSTRAINT `islands_warp_FK` FOREIGN KEY (`island_id`) REFERENCES `islands` (`island_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String CREATE_SPIRAL = """
                CREATE TABLE IF NOT EXISTS `%s`.`spiral` (
                  `id` INT NOT NULL,
                  `region_x` INT NOT NULL,
                  `region_z` INT NOT NULL,
                  PRIMARY KEY (`id`),
                  INDEX `idx_region_x` (`region_x`),
                  INDEX `idx_region_z` (`region_z`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private static final String CREATE_TABLE_ISLAND_PERMISSION = """
            CREATE TABLE IF NOT EXISTS %s.`islands_permissions` (
            `island_id` VARCHAR(36) NOT NULL,
            `type` VARCHAR(36) NOT NULL,
            `role` VARCHAR(40) NOT NULL,
            `flags` INT UNSIGNED NOT NULL DEFAULT '0',
            PRIMARY KEY (`island_id`,`type`,`role`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String CREATE_TABLE_CLEAR_INVENTORY_CAUSE_KICK = """
                CREATE TABLE IF NOT EXISTS `%s`.`player_clear` (
                   `uuid_player` CHAR(36) NOT NULL,
                   PRIMARY KEY (`uuid_player`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String INSERT_SPIRAL = """
                INSERT IGNORE INTO `%s`.`spiral`
                (id, region_x, region_z)
                VALUES(?, ?, ?);
            """;
    private final Logger logger = LogManager.getLogger(this);
    private final String database;
    private final InterneAPI api;
    private final int dbVersion;

    public MariaDBCreateTable(InterneAPI interneAPI) throws DatabaseException {
        this.api = interneAPI;
        MariaDBConfig dbConfig = ConfigToml.mariaDBConfig;
        if (dbConfig == null) {
            throw new DatabaseException("No database is mentioned in the configuration of the plugin.", null);
        }
        this.database = dbConfig.database();
        this.dbVersion = dbConfig.dbVersion();
        try {
            this.init();
        } catch (SQLException e) {
            logger.log(Level.FATAL, e);
            throw new DatabaseException(e);
        }
    }

    private void init() throws SQLException {
        // DATABASE
        MariaDBExecute.executeQuery(api, CREATE_DATABASE.formatted(this.database));
        MariaDBExecute.executeQuery(api, CREATE_ISLANDS.formatted(this.database));
        if (this.dbVersion <= 1) {
            MariaDBExecute.executeQuery(api, "ALTER TABLE `%s`.`islands` MODIFY `size` DOUBLE;".formatted(this.database));
        }
        MariaDBExecute.executeQuery(api, CREATE_ISLANDS_MEMBERS.formatted(this.database));
        MariaDBExecute.executeQuery(api, CREATE_ISLANDS_WARP.formatted(this.database));
        MariaDBExecute.executeQuery(api, CREATE_SPIRAL.formatted(this.database));
        MariaDBExecute.executeQuery(api, CREATE_TABLE_CLEAR_INVENTORY_CAUSE_KICK.formatted(this.database));
        MariaDBExecute.executeQuery(api, CREATE_TABLE_ISLAND_PERMISSION.formatted(this.database));
        ExecutorService scheduledExecutorService = Executors.newCachedThreadPool();
        try {
            scheduledExecutorService.execute(() -> {
                for (int i = 1; i < ConfigToml.maxIsland; i++) {
                    Position position = RegionUtils.getPositionNewIsland(i);
                    MariaDBExecute.executeQuery(api, INSERT_SPIRAL.formatted(this.database), List.of(i, position.regionX(), position.regionZ()), null, null);
                    if (i % 1000 == 0) {
                        logger.log(Level.INFO, "Insertion en cours (" + i + "/" + ConfigToml.maxIsland + ")");
                    }
                }
            });
        } finally {
            scheduledExecutorService.shutdown();
        }
    }
}
