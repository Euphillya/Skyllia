package fr.euphyllia.skyfolia.database.query;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.configuration.section.MariaDBConfig;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
import fr.euphyllia.skyfolia.utils.exception.DatabaseException;

import java.sql.SQLException;

public class MariaDBCreateTable {

    private static final String CREATE_DATABASE = """
            CREATE DATABASE IF NOT EXISTS `%s`;
            """;

    private static final String CREATE_ISLANDS = """
             CREATE TABLE IF NOT EXISTS `%s`.`islands` (
             `island_id` VARCHAR(36) NOT NULL,
             `enable` TINYINT DEFAULT '1',
             `region_x` INT NOT NULL,
             `region_z` INT NOT NULL,
             `private` TINYINT DEFAULT '0'
             ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
             """;

    private static final String CREATE_ISLANDS_MEMBERS = """
                CREATE TABLE IF NOT EXISTS `%s`.`members_in_islands` (
                  `island_id` varchar(36) NOT NULL,
                  `uuid_player` varchar(36) NOT NULL,
                  `role` varchar(40) DEFAULT NULL,
                  PRIMARY KEY (`island_id`,`uuid_player`),
                  CONSTRAINT `members_in_islands_FK` FOREIGN KEY (`island_id`) REFERENCES `islands` (`island_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private static final String CREATE_ISLANDS_WARP = """
                CREATE TABLE IF NOT EXISTS `%s`.`islands_warp` (
                  `id` int unsigned NOT NULL AUTO_INCREMENT,
                  `island_id` varchar(36) NOT NULL,
                  `warp_name` varchar(100) DEFAULT NULL,
                  `world_name` varchar(100) DEFAULT NULL,
                  `x` int DEFAULT NULL,
                  `y` int DEFAULT NULL,
                  `z` int DEFAULT NULL,
                  `pitch` FLOAT DEFAULT NULL,
                  `yaw` FLOAT DEFAULT NULL,
                  PRIMARY KEY (`id`),
                  KEY `islands_warp_FK` (`island_id`),
                  CONSTRAINT `islands_warp_FK` FOREIGN KEY (`island_id`) REFERENCES `islands` (`island_id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private final String database;
    private final InterneAPI api;

    public MariaDBCreateTable(InterneAPI interneAPI) throws DatabaseException {
        this.api = interneAPI;
        MariaDBConfig dbConfig = ConfigToml.mariaDBConfig;
        if (dbConfig == null) {
            throw new DatabaseException("No database is mentioned in the configuration of the plugin.", null);
        }
        this.database = ConfigToml.mariaDBConfig.database();
        try {
            this.init();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void init() throws SQLException {
        // DATABASE
        MariaDBExecute.executeQuery(api, CREATE_DATABASE.formatted(this.database));
        MariaDBExecute.executeQuery(api, CREATE_ISLANDS.formatted(this.database));
        MariaDBExecute.executeQuery(api, CREATE_ISLANDS_MEMBERS.formatted(this.database));
        MariaDBExecute.executeQuery(api, CREATE_ISLANDS_WARP.formatted(this.database));
    }
}
