package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.api.skyblock.IslandData;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.configuration.MariaDBConfig;
import fr.euphyllia.skyllia.sgbd.utils.sql.execute.SQLExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class MariaDBDatabaseInitialize extends DatabaseInitializeQuery {

    private static final String CREATE_DATABASE_TEMPLATE = "CREATE DATABASE IF NOT EXISTS `%s`;";
    private static final String CREATE_ISLANDS_TABLE = """
            CREATE TABLE IF NOT EXISTS `%s`.`islands` (
                `island_id` CHAR(36) NOT NULL,
                `disable` TINYINT DEFAULT '0',
                `region_x` INT NOT NULL,
                `region_z` INT NOT NULL,
                `private` TINYINT DEFAULT '0',
                `size` DOUBLE NOT NULL,
                `create_time` TIMESTAMP,
                `max_members` INT NOT NULL,
                PRIMARY KEY (`island_id`, `region_x`, `region_z`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String CREATE_ISLANDS_GAMERULE_TABLE = """
            CREATE TABLE IF NOT EXISTS `%s`.`islands_gamerule` (
                `island_id` CHAR(36) NOT NULL,
                `flags` INT UNSIGNED NOT NULL DEFAULT '0',
                PRIMARY KEY (`island_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String CREATE_ISLANDS_MEMBERS_TABLE = """
            CREATE TABLE IF NOT EXISTS `%s`.`members_in_islands` (
                `island_id` CHAR(36) NOT NULL,
                `uuid_player` CHAR(36) NOT NULL,
                `player_name` VARCHAR(40) DEFAULT NULL,
                `role` VARCHAR(40) DEFAULT NULL,
                `joined` TIMESTAMP,
                PRIMARY KEY (`island_id`, `uuid_player`),
                CONSTRAINT `members_in_islands_FK` FOREIGN KEY (`island_id`) REFERENCES `islands` (`island_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String CREATE_ISLANDS_WARP_TABLE = """
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
    private static final String CREATE_SPIRAL_TABLE = """
            CREATE TABLE IF NOT EXISTS `%s`.`spiral` (
                `id` INT NOT NULL,
                `region_x` INT NOT NULL,
                `region_z` INT NOT NULL,
                PRIMARY KEY (`id`),
                INDEX `idx_region_x` (`region_x`),
                INDEX `idx_region_z` (`region_z`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String CREATE_ISLANDS_PERMISSIONS_TABLE = """
            CREATE TABLE IF NOT EXISTS `%s`.`islands_permissions` (
                `island_id` VARCHAR(36) NOT NULL,
                `type` VARCHAR(36) NOT NULL,
                `role` VARCHAR(40) NOT NULL,
                `flags` INT UNSIGNED NOT NULL DEFAULT '0',
                PRIMARY KEY (`island_id`, `type`, `role`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String CREATE_PLAYER_CLEAR_TABLE = """
            CREATE TABLE IF NOT EXISTS `%s`.`player_clear` (
                `uuid_player` CHAR(36) NOT NULL,
                `cause` VARCHAR(50) NOT NULL DEFAULT 'ISLAND_DELETED',
                PRIMARY KEY (`uuid_player`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static final String INSERT_SPIRAL = """
            INSERT IGNORE INTO `%s`.`spiral` (id, region_x, region_z) VALUES (?, ?, ?);
            """;
    private static final String CREATE_ISLANDS_INDEX = """
            CREATE INDEX IF NOT EXISTS `region_xz_disabled` ON `%s`.`islands` (`region_x`, `region_z`, `disable`);
            """;
    private static final String CREATE_SPIRAL_INDEX = """
            CREATE INDEX IF NOT EXISTS `region_xz` ON `%s`.`spiral` (`region_x`, `region_z`);
            """;
    private static final String CREATE_MEMBERS_BY_PLAYER_INDEX = """
            CREATE INDEX IF NOT EXISTS `idx_member_by_player` ON `%s`.`members_in_islands` (`uuid_player`, `role`, `island_id`);
            """;
    private static final String CREATE_MEMBER_BY_ISLAND_ROLE_INDEX = """
            CREATE INDEX IF NOT EXISTS `idx_member_by_island_role` ON `%s`.`members_in_islands` (`island_id`, `role`, `uuid_player`);
            """;

    private final Logger logger = LogManager.getLogger(MariaDBDatabaseInitialize.class);
    private final String database;
    private final InterneAPI api;

    public MariaDBDatabaseInitialize(InterneAPI interneAPI) throws DatabaseException {
        this.api = interneAPI;
        MariaDBConfig dbConfig = ConfigLoader.database.getMariaDBConfig();

        if (dbConfig == null) {
            throw new DatabaseException("No database is mentioned in the configuration of the plugin.", null);
        }
        this.database = dbConfig.database();
    }

    @Override
    public boolean init() throws DatabaseException {
        // Create Database and Tables
        createDatabaseAndTables();

        // Apply necessary migrations based on database version
        applyMigrations();

        // Initialize Spiral Table
        initializeSpiralTable();

        return true;
    }

    private void createDatabaseAndTables() throws DatabaseException {
        executeQuery(CREATE_DATABASE_TEMPLATE.formatted(database));
        executeQuery(CREATE_ISLANDS_TABLE.formatted(database));
        executeQuery(CREATE_ISLANDS_MEMBERS_TABLE.formatted(database));
        executeQuery(CREATE_ISLANDS_WARP_TABLE.formatted(database));
        executeQuery(CREATE_SPIRAL_TABLE.formatted(database));
        executeQuery(CREATE_ISLANDS_PERMISSIONS_TABLE.formatted(database));
        executeQuery(CREATE_PLAYER_CLEAR_TABLE.formatted(database));
        executeQuery(CREATE_ISLANDS_GAMERULE_TABLE.formatted(database));
        executeQuery(CREATE_ISLANDS_INDEX.formatted(database));
        executeQuery(CREATE_SPIRAL_INDEX.formatted(database));
        executeQuery(CREATE_MEMBERS_BY_PLAYER_INDEX.formatted(database));
        executeQuery(CREATE_MEMBER_BY_ISLAND_ROLE_INDEX.formatted(database));
    }

    private void applyMigrations() throws DatabaseException {
        if (ConfigLoader.database.getConfigVersion() <= 1) {
            executeQuery("ALTER TABLE `%s`.`islands` MODIFY `size` DOUBLE;".formatted(database));
            executeQuery("""
                    ALTER TABLE `%s`.`islands_gamerule` DROP PRIMARY KEY,
                    ADD PRIMARY KEY (`island_id`) USING BTREE;
                    """.formatted(database));
            executeQuery("ALTER TABLE `%s`.`islands` ADD COLUMN IF NOT EXISTS `locked` TINYINT(1) NOT NULL DEFAULT 0;".formatted(database));
        }
    }

    private void initializeSpiralTable() {
        int distancePerIsland = ConfigLoader.general.getRegionDistance();
        if (distancePerIsland <= 0) {
            logger.log(Level.FATAL, "You must set a value greater than 1 for region distance per island (config/config.toml -> settings.island.region-distance). " +
                    "If you're using an earlier version of the plugin, set the value to 1 to avoid any bugs, otherwise increase the distance.");
            return;
        }

        Runnable spiralTask = () -> {
            List<IslandData> islandDataList = new ArrayList<>();
            for (int i = 1; i < ConfigLoader.general.getMaxIslands(); i++) {
                Position position = RegionUtils.computeNewIslandRegionPosition(i);
                islandDataList.add(new IslandData(
                        i,
                        position.x() * distancePerIsland,
                        position.z() * distancePerIsland
                ));
            }

            SpiralBatchInserter batchInserter = new SpiralBatchInserter(
                    String.format(INSERT_SPIRAL, database),
                    islandDataList
            );

            try {
                SQLExecute.executeQueryDML(
                        api.getDatabaseLoader(),
                        String.format(INSERT_SPIRAL, database),
                        null,
                        null,
                        batchInserter
                );
            } catch (DatabaseException e) {
                logger.log(Level.ERROR, "Error inserting into spiral table", e);
            }
        };

        executeAsync(spiralTask);
    }

    private void executeQuery(String query) throws DatabaseException {
        SQLExecute.executeQuery(api.getDatabaseLoader(), query);
    }

    private void executeQuery(String query, List<Object> params) throws DatabaseException {
        SQLExecute.executeQuery(api.getDatabaseLoader(), query, params, null, null);
    }

    private void executeAsync(Runnable task) {
        Bukkit.getAsyncScheduler().runNow(api.getPlugin(), scheduledTask -> {
            task.run();
        });
    }
}
