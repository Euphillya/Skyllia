package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.api.skyblock.IslandData;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MariaDBDatabaseInitialize extends DatabaseInitializeQuery {

    private static final Logger logger = LogManager.getLogger(MariaDBDatabaseInitialize.class);

    private static final String CREATE_ISLANDS_TABLE = """
            CREATE TABLE IF NOT EXISTS islands (
                island_id CHAR(36) NOT NULL,
                disable TINYINT DEFAULT 0,
                region_x INT NOT NULL,
                region_z INT NOT NULL,
                private TINYINT DEFAULT 0,
                size DOUBLE NOT NULL,
                create_time TIMESTAMP,
                max_members INT NOT NULL,
                PRIMARY KEY (island_id, region_x, region_z)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private static final String CREATE_ISLANDS_GAMERULE_TABLE = """
            CREATE TABLE IF NOT EXISTS islands_gamerule (
                island_id CHAR(36) NOT NULL,
                flags INT UNSIGNED NOT NULL DEFAULT 0,
                PRIMARY KEY (island_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private static final String CREATE_ISLANDS_MEMBERS_TABLE = """
            CREATE TABLE IF NOT EXISTS members_in_islands (
                island_id CHAR(36) NOT NULL,
                uuid_player CHAR(36) NOT NULL,
                player_name VARCHAR(40) DEFAULT NULL,
                role VARCHAR(40) DEFAULT NULL,
                joined TIMESTAMP,
                PRIMARY KEY (island_id, uuid_player),
                CONSTRAINT members_in_islands_FK FOREIGN KEY (island_id) REFERENCES islands (island_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private static final String CREATE_ISLANDS_WARP_TABLE = """
            CREATE TABLE IF NOT EXISTS islands_warp (
                id INT UNSIGNED NOT NULL AUTO_INCREMENT,
                island_id CHAR(36) NOT NULL,
                warp_name VARCHAR(100) DEFAULT NULL,
                world_name VARCHAR(100) DEFAULT NULL,
                x DOUBLE DEFAULT NULL,
                y DOUBLE DEFAULT NULL,
                z DOUBLE DEFAULT NULL,
                pitch FLOAT DEFAULT NULL,
                yaw FLOAT DEFAULT NULL,
                PRIMARY KEY (id),
                UNIQUE KEY unique_warp_per_island (island_id, warp_name),
                CONSTRAINT islands_warp_FK FOREIGN KEY (island_id) REFERENCES islands (island_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private static final String CREATE_SPIRAL_TABLE = """
            CREATE TABLE IF NOT EXISTS spiral (
                id INT NOT NULL,
                region_x INT NOT NULL,
                region_z INT NOT NULL,
                PRIMARY KEY (id),
                INDEX idx_region_x (region_x),
                INDEX idx_region_z (region_z)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private static final String CREATE_ISLANDS_PERMISSIONS_TABLE = """
            CREATE TABLE IF NOT EXISTS islands_permissions (
                island_id VARCHAR(36) NOT NULL,
                type VARCHAR(36) NOT NULL,
                role VARCHAR(40) NOT NULL,
                flags INT UNSIGNED NOT NULL DEFAULT 0,
                PRIMARY KEY (island_id, type, role)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private static final String CREATE_PLAYER_CLEAR_TABLE = """
            CREATE TABLE IF NOT EXISTS player_clear (
                uuid_player CHAR(36) NOT NULL,
                cause VARCHAR(50) NOT NULL DEFAULT 'ISLAND_DELETED',
                PRIMARY KEY (uuid_player)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;

    private static final String INSERT_SPIRAL = """
            INSERT IGNORE INTO spiral (id, region_x, region_z) VALUES (?, ?, ?);
            """;

    private static final String CREATE_ISLANDS_INDEX = """
            CREATE INDEX IF NOT EXISTS region_xz_disabled
            ON islands (region_x, region_z, disable);
            """;

    private static final String CREATE_SPIRAL_INDEX = """
            CREATE INDEX IF NOT EXISTS region_xz
            ON spiral (region_x, region_z);
            """;

    private static final String CREATE_MEMBERS_BY_PLAYER_INDEX = """
            CREATE INDEX IF NOT EXISTS idx_member_by_player
            ON members_in_islands (uuid_player, role, island_id);
            """;

    private static final String CREATE_MEMBER_BY_ISLAND_ROLE_INDEX = """
            CREATE INDEX IF NOT EXISTS idx_member_by_island_role
            ON members_in_islands (island_id, role, uuid_player);
            """;
    public final int regionDistance;
    public final int maxIslands;
    private final DatabaseLoader databaseLoader;
    private final int configVersion;

    public MariaDBDatabaseInitialize(@NotNull DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
        this.configVersion = ConfigLoader.database.getConfigVersion();
        this.regionDistance = ConfigLoader.general.getRegionDistance();
        this.maxIslands = ConfigLoader.general.getMaxIslands();
    }

    @Override
    public Boolean init() {
        createDatabaseAndTables();
        applyMigrations();
        initializeSpiralTable();
        return true;
    }

    private void createDatabaseAndTables() {
        exec(CREATE_ISLANDS_TABLE);
        exec(CREATE_ISLANDS_MEMBERS_TABLE);
        exec(CREATE_ISLANDS_WARP_TABLE);
        exec(CREATE_SPIRAL_TABLE);
        exec(CREATE_ISLANDS_PERMISSIONS_TABLE);
        exec(CREATE_PLAYER_CLEAR_TABLE);
        exec(CREATE_ISLANDS_GAMERULE_TABLE);
        exec(CREATE_ISLANDS_INDEX);
        exec(CREATE_SPIRAL_INDEX);
        exec(CREATE_MEMBERS_BY_PLAYER_INDEX);
        exec(CREATE_MEMBER_BY_ISLAND_ROLE_INDEX);
    }

    private void applyMigrations() {
        if (configVersion <= 1) {
            exec("ALTER TABLE islands MODIFY size DOUBLE;");
            exec("""
                    ALTER TABLE islands_gamerule
                    DROP PRIMARY KEY,
                    ADD PRIMARY KEY (island_id) USING BTREE;
                    """);
        }

        exec("ALTER TABLE islands ADD COLUMN IF NOT EXISTS locked TINYINT(1) NOT NULL DEFAULT 0;");
    }

    private void initializeSpiralTable() {
        if (regionDistance <= 0) {
            logger.log(Level.FATAL, "Invalid region distance.");
            return;
        }

        Runnable spiralTask = () -> {
            List<IslandData> islandDataList = new ArrayList<>();
            for (int i = 1; i < maxIslands; i++) {
                Position pos = RegionUtils.computeNewIslandRegionPosition(i);
                islandDataList.add(new IslandData(
                        i,
                        pos.x() * regionDistance,
                        pos.z() * regionDistance
                ));
            }

            SQLExecute.work(databaseLoader, connection ->
                    new SpiralBatchInserter(INSERT_SPIRAL, islandDataList).run(connection)
            );
        };

        Bukkit.getAsyncScheduler().runNow(SkylliaAPI.getPlugin(), t -> spiralTask.run());
    }

    private void exec(String sql) {
        SQLExecute.update(databaseLoader, sql, null);
    }
}
