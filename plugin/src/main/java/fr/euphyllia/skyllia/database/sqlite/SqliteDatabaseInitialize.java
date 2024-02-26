package fr.euphyllia.skyllia.database.sqlite;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.database.execute.MariaDBExecute;
import fr.euphyllia.skyllia.api.exceptions.DatabaseException;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.api.utils.scheduler.SchedulerTask;
import fr.euphyllia.skyllia.api.utils.scheduler.model.SchedulerType;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.database.query.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.utils.RegionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class SqliteDatabaseInitialize extends DatabaseInitializeQuery {

    private static final String CREATE_ISLANDS = """
            CREATE TABLE IF NOT EXISTS islands (
                island_id TEXT NOT NULL,
                disable INTEGER DEFAULT 0,
                region_x INTEGER NOT NULL,
                region_z INTEGER NOT NULL,
                private INTEGER DEFAULT 0,
                size REAL NOT NULL,
                create_time TIMESTAMP,
                max_members INTEGER NOT NULL,
                PRIMARY KEY (island_id, region_x, region_z)
            );
            """;

    private static final String CREATE_GAMERULE_ISLANDS = """
            CREATE TABLE IF NOT EXISTS islands_gamerule (
                island_id TEXT NOT NULL,
                flags INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (island_id)
            );
            """;
    private static final String CREATE_ISLANDS_MEMBERS = """
                CREATE TABLE IF NOT EXISTS members_in_islands (
                    island_id TEXT NOT NULL,
                    uuid_player TEXT NOT NULL,
                    player_name TEXT DEFAULT NULL,
                    role TEXT DEFAULT NULL,
                    joined TIMESTAMP,
                    PRIMARY KEY (island_id, uuid_player),
                    FOREIGN KEY (island_id) REFERENCES islands (island_id)
                );
            """;
    private static final String CREATE_ISLANDS_WARP = """
                CREATE TABLE IF NOT EXISTS islands_warp (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    island_id TEXT NOT NULL,
                    warp_name TEXT DEFAULT NULL,
                    world_name TEXT DEFAULT NULL,
                    x REAL DEFAULT NULL,
                    y REAL DEFAULT NULL,
                    z REAL DEFAULT NULL,
                    pitch REAL DEFAULT NULL,
                    yaw REAL DEFAULT NULL,
                    UNIQUE (island_id, warp_name),
                    FOREIGN KEY (island_id) REFERENCES islands (island_id)
                );
            """;
    private static final String CREATE_SPIRAL = """
                CREATE TABLE IF NOT EXISTS spiral (
                    id INTEGER NOT NULL,
                    region_x INTEGER NOT NULL,
                    region_z INTEGER NOT NULL,
                    PRIMARY KEY (id)
                );
            """;
    private static final String SPIRAL_INDEX1 = "CREATE INDEX IF NOT EXISTS idx_region_x ON spiral (region_x);";
    private static final String SPIRAL_INDEX2 = "CREATE INDEX IF NOT EXISTS idx_region_z ON spiral (region_z);";

    private static final String CREATE_TABLE_ISLAND_PERMISSION = """
            CREATE TABLE IF NOT EXISTS islands_permissions (
                island_id TEXT NOT NULL,
                type TEXT NOT NULL,
                role TEXT NOT NULL,
                flags INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (island_id, type, role)
            );
            """;
    private static final String CREATE_TABLE_CLEAR_INVENTORY_CAUSE_KICK = """
            CREATE TABLE IF NOT EXISTS player_clear (
                uuid_player TEXT NOT NULL,
                PRIMARY KEY (uuid_player)
            );
            """;
    private static final String INSERT_SPIRAL = """
            INSERT OR IGNORE INTO spiral (id, region_x, region_z)
            VALUES (?, ?, ?);
            """;

    private final Logger logger = LogManager.getLogger(SqliteDatabaseInitialize.class);
    private final InterneAPI api;

    public SqliteDatabaseInitialize(InterneAPI interneAPI) throws DatabaseException {
        this.api = interneAPI;
    }

    @Override
    public boolean init() throws DatabaseException {
        // DATABASE

        MariaDBExecute.executeQuery(api.getDatabaseLoader(), CREATE_ISLANDS);
        MariaDBExecute.executeQuery(api.getDatabaseLoader(), CREATE_ISLANDS);
        MariaDBExecute.executeQuery(api.getDatabaseLoader(), CREATE_ISLANDS_MEMBERS);
        MariaDBExecute.executeQuery(api.getDatabaseLoader(), CREATE_ISLANDS_WARP);
        MariaDBExecute.executeQuery(api.getDatabaseLoader(), CREATE_SPIRAL);
        MariaDBExecute.executeQuery(api.getDatabaseLoader(), SPIRAL_INDEX1);
        MariaDBExecute.executeQuery(api.getDatabaseLoader(), SPIRAL_INDEX2);
        MariaDBExecute.executeQuery(api.getDatabaseLoader(), CREATE_TABLE_CLEAR_INVENTORY_CAUSE_KICK);
        MariaDBExecute.executeQuery(api.getDatabaseLoader(), CREATE_TABLE_ISLAND_PERMISSION);
        MariaDBExecute.executeQuery(api.getDatabaseLoader(), CREATE_GAMERULE_ISLANDS);
        int distancePerIsland = ConfigToml.regionDistance;
        if (distancePerIsland <= 0) {
            logger.log(Level.FATAL, "You must set a value greater than 1 distance region file per island (config.toml -> config.region-distance-per-island). " +
                    "If you're using an earlier version of the plugin, set the value to 1 to avoid any bugs, otherwise increase the distance.");
            return false;
        }

        SkylliaAPI.getSchedulerTask()
                .getScheduler(SchedulerTask.SchedulerSoft.NATIVE)
                .runDelayed(SchedulerType.ASYNC, 1, schedulerTask -> {
                    for (int i = 1; i < ConfigToml.maxIsland; i++) {
                        Position position = RegionUtils.getPositionNewIsland(i);
                        try {
                            MariaDBExecute.executeQuery(api.getDatabaseLoader(), INSERT_SPIRAL, List.of(i, position.x() * distancePerIsland, position.z() * distancePerIsland), null, null, false);
                        } catch (DatabaseException e) {
                            return; // ignore
                        }
                    }
                });
        return true;
    }
}
