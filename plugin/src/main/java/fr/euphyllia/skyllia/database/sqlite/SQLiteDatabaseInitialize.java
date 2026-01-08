package fr.euphyllia.skyllia.database.sqlite;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.RegionUtils;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

public class SQLiteDatabaseInitialize extends DatabaseInitializeQuery {

    private static final Logger logger = LogManager.getLogger(SQLiteDatabaseInitialize.class);

    private static final String CREATE_ISLANDS_TABLE = """
            CREATE TABLE IF NOT EXISTS islands (
                island_id TEXT NOT NULL,
                region_x  INTEGER NOT NULL,
                region_z  INTEGER NOT NULL,
                disable   INTEGER DEFAULT 0,
                private   INTEGER DEFAULT 0,
                locked    INTEGER DEFAULT 0,
                size      REAL NOT NULL,
                create_time TEXT,
                max_members INTEGER NOT NULL,
                PRIMARY KEY (island_id, region_x, region_z),
                UNIQUE(island_id)
            );
            """;

    private static final String CREATE_ISLANDS_GAMERULE_TABLE = """
            CREATE TABLE IF NOT EXISTS islands_gamerule (
                island_id TEXT NOT NULL,
                flags INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (island_id)
            );
            """;

    private static final String CREATE_ISLANDS_MEMBERS_TABLE = """
            CREATE TABLE IF NOT EXISTS members_in_islands (
                island_id   TEXT NOT NULL,
                uuid_player TEXT NOT NULL,
                player_name TEXT DEFAULT NULL,
                role        TEXT DEFAULT NULL,
                joined      TEXT,
                PRIMARY KEY (island_id, uuid_player),
                FOREIGN KEY(island_id) REFERENCES islands(island_id)
            );
            """;

    private static final String CREATE_ISLANDS_WARP_TABLE = """
            CREATE TABLE IF NOT EXISTS islands_warp (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                island_id TEXT NOT NULL,
                warp_name TEXT DEFAULT NULL,
                world_name TEXT DEFAULT NULL,
                x REAL DEFAULT NULL,
                y REAL DEFAULT NULL,
                z REAL DEFAULT NULL,
                pitch REAL DEFAULT NULL,
                yaw REAL DEFAULT NULL,
                UNIQUE(island_id, warp_name)
            );
            """;

    private static final String CREATE_SPIRAL_TABLE = """
            CREATE TABLE IF NOT EXISTS spiral (
                id INTEGER NOT NULL,
                region_x INTEGER NOT NULL,
                region_z INTEGER NOT NULL,
                PRIMARY KEY (id)
            );
            """;

    private static final String CREATE_ISLANDS_PERMISSIONS_TABLE = """
            CREATE TABLE IF NOT EXISTS islands_permissions_v2 (
                  island_id TEXT NOT NULL,
                  role TEXT NOT NULL,
                  words BLOB NOT NULL,
                  PRIMARY KEY (island_id, role)
            );
            """;

    private static final String CREATE_PLAYER_CLEAR_TABLE = """
            CREATE TABLE IF NOT EXISTS player_clear (
                uuid_player TEXT NOT NULL,
                cause TEXT NOT NULL DEFAULT 'ISLAND_DELETED',
                PRIMARY KEY (uuid_player, cause)
            );
            """;

    private static final String INSERT_SPIRAL = """
            INSERT OR IGNORE INTO spiral (id, region_x, region_z) VALUES (?, ?, ?);
            """;

    private static final String CREATE_ISLANDS_INDEX = """
            CREATE INDEX IF NOT EXISTS region_xz_disabled
                ON islands (region_x, region_z, disable);
            """;

    private static final String CREATE_SPIRAL_INDEX = """
            CREATE INDEX IF NOT EXISTS region_xz
                ON spiral (region_x, region_z);
            """;

    private final DatabaseLoader databaseLoader;

    public SQLiteDatabaseInitialize(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
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
    }

    private void applyMigrations() {
        exec("ALTER TABLE islands ADD COLUMN locked INTEGER DEFAULT 0;");
    }

    private void initializeSpiralTable() {
        int distancePerIsland = ConfigLoader.general.getRegionDistance();
        if (distancePerIsland <= 0) {
            logger.log(Level.FATAL,
                    "You must set a value greater than 1 for region distance per island (config/config.toml -> settings.island.region-distance). " +
                            "If you're using an earlier version of the plugin, set the value to 1 to avoid any bugs, otherwise increase the distance.");
            return;
        }

        Runnable spiralTask = () -> {
            List<SQLiteSpiralBatchInserter.IslandData> islandDataList = new ArrayList<>();
            for (int i = 1; i < ConfigLoader.general.getMaxIslands(); i++) {
                Position position = RegionUtils.computeNewIslandRegionPosition(i);
                islandDataList.add(new SQLiteSpiralBatchInserter.IslandData(
                        i,
                        position.x() * distancePerIsland,
                        position.z() * distancePerIsland
                ));
            }

            SQLiteSpiralBatchInserter batchInserter = new SQLiteSpiralBatchInserter(INSERT_SPIRAL, islandDataList);

            SQLExecute.work(databaseLoader, batchInserter::run);
        };

        Bukkit.getAsyncScheduler().runNow(SkylliaAPI.getPlugin(), task -> spiralTask.run());
    }

    private void exec(String sql) {
        SQLExecute.update(databaseLoader, sql, null);
    }
}
