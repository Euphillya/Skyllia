package fr.euphyllia.skyllia.database.sqlite;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.coordinate.RegionCoordinate;
import fr.euphyllia.skyllia.api.database.IslandDataQuery;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.managers.skyblock.IslandHook;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SQLiteIslandData extends IslandDataQuery {

    private static final Logger logger = LogManager.getLogger(SQLiteIslandData.class);

    private static final String SELECT_ISLAND_BY_OWNER = """
            SELECT i.*
            FROM islands i
            JOIN members_in_islands mi ON i.island_id = mi.island_id
            WHERE mi.role = 'OWNER'
              AND mi.uuid_player = ?
              AND i.disable = 0;
            """;

    private static final String SELECT_ISLAND_BY_PLAYER_ID = """
            SELECT i.*
            FROM islands i
            JOIN members_in_islands mi ON i.island_id = mi.island_id
            WHERE mi.role NOT IN ('BAN', 'VISITOR')
              AND mi.uuid_player = ?
              AND i.disable = 0
            LIMIT 1;
            """;

    private static final String SELECT_ISLAND_BY_ISLAND_ID = """
            SELECT island_id, disable, region_x, region_z, private, size, create_time, max_members
            FROM islands
            WHERE island_id = ?;
            """;

    private static final String ADD_ISLANDS = """
            INSERT INTO islands (
                island_id, disable, region_x, region_z, private, size, create_time, max_members
            )
            SELECT
                ?,                       -- 1) island_id
                0,                       -- disable
                s.region_x,
                s.region_z,
                ?,                       -- 2) private (0/1)
                ?,                       -- 3) size (REAL)
                DATETIME('now'),         -- create_time
                ?                        -- 4) max_members
            FROM spiral s
            LEFT JOIN islands i
              ON s.region_x = i.region_x
             AND s.region_z = i.region_z
             AND (i.locked = 1 OR i.disable = 0)
            WHERE i.region_x IS NULL
            ORDER BY s.id
            LIMIT 1;
            """;

    private static final String SELECT_ALL_ISLANDS_VALID = """
            SELECT island_id, disable, region_x, region_z, private, size, create_time, max_members
            FROM islands
            WHERE disable = 0;
            """;

    private static final String SELECT_ISLAND_BY_POSITION_VALID = """
            SELECT island_id, disable, region_x, region_z, private, size, create_time, max_members
            FROM islands
            WHERE region_x = ?
              AND region_z = ?
              AND disable = 0
              AND locked = 0
            LIMIT 1;
            """;

    private static final String UPSERT_CENTER_LOCATION = """
            INSERT INTO island_center_locations (island_id, world_name, center_x, center_y, center_z)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(island_id, world_name) DO UPDATE SET
                center_x = excluded.center_x,
                center_y = excluded.center_y,
                center_z = excluded.center_z;
            """;

    private static final String SELECT_CENTER_LOCATIONS = """
            SELECT world_name, center_x, center_y, center_z
            FROM island_center_locations
            WHERE island_id = ?;
            """;


    private final DatabaseLoader databaseLoader;

    public SQLiteIslandData(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    /**
     * SQLite DATETIME('now') renvoie souvent "YYYY-MM-DD HH:MM:SS"
     */
    private static @Nullable Timestamp parseSqliteTimestamp(@Nullable String timeString) {
        if (timeString == null || timeString.isBlank()) return null;
        try {
            LocalDateTime ldt = LocalDateTime.parse(timeString.replace(' ', 'T'));
            return Timestamp.valueOf(ldt);
        } catch (Exception ignored) {
            try {
                return Timestamp.valueOf(timeString);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    public @Nullable Island getIslandByOwnerId(UUID playerId) {
        Island island = SQLExecute.queryMap(databaseLoader, SELECT_ISLAND_BY_OWNER, List.of(playerId.toString()), rs -> {
            try {
                if (rs.next()) {
                    return constructIslandQuery(rs);
                }
            } catch (Exception e) {
                logger.log(Level.ERROR, "getIslandByOwnerId failed", e);
            }
            return null;
        });

        if (island != null) {
            Bukkit.getAsyncScheduler().runNow(SkylliaAPI.getPlugin(), scheduledTask -> new SkyblockLoadEvent(island).callEvent());
        }
        return island;
    }

    @Override
    public @Nullable Island getIslandByPlayerId(UUID playerId) {
        Island island = SQLExecute.queryMap(databaseLoader, SELECT_ISLAND_BY_PLAYER_ID, List.of(playerId.toString()), rs -> {
            try {
                if (rs.next()) {
                    return constructIslandQuery(rs);
                }
            } catch (Exception e) {
                logger.log(Level.ERROR, "getIslandByPlayerId failed", e);
            }
            return null;
        });

        if (island != null) {
            Bukkit.getAsyncScheduler().runNow(SkylliaAPI.getPlugin(), scheduledTask -> new SkyblockLoadEvent(island).callEvent());
        }
        return island;
    }

    @Override
    public Boolean insertIslands(Island futurIsland) {
        int affected = SQLExecute.update(databaseLoader, ADD_ISLANDS, List.of(
                futurIsland.getId().toString(),
                futurIsland.isPrivateIsland() ? 1 : 0,
                futurIsland.getSize(),
                futurIsland.getMaxMembers()
        ));
        return affected > 0;
    }

    @Override
    public @Nullable Island getIslandByIslandId(UUID islandId) {
        return SQLExecute.queryMap(databaseLoader, SELECT_ISLAND_BY_ISLAND_ID, List.of(islandId.toString()), rs -> {
            try {
                if (rs.next()) {
                    return constructIslandQuery(rs);
                }
            } catch (Exception e) {
                logger.log(Level.ERROR, "getIslandByIslandId failed", e);
            }
            return null;
        });
    }

    @Override
    public List<Island> getAllIslandsValid() {
        List<Island> islands = SQLExecute.queryMap(databaseLoader, SELECT_ALL_ISLANDS_VALID, null, rs -> {
            List<Island> out = new ArrayList<>();
            try {
                while (rs.next()) {
                    Island island = constructIslandQuery(rs);
                    if (island != null) out.add(island);
                }
            } catch (Exception e) {
                logger.log(Level.ERROR, "getAllIslandsValid failed", e);
            }
            return out;
        });

        return islands != null ? islands : List.of();
    }

    @Override
    public Integer getMaxMemberInIsland(Island island) {
        Integer max = SQLExecute.queryMap(databaseLoader, SELECT_ISLAND_BY_ISLAND_ID, List.of(island.getId().toString()), rs -> {
            try {
                if (rs.next()) return rs.getInt("max_members");
            } catch (SQLException e) {
                logger.log(Level.ERROR, "getMaxMemberInIsland failed", e);
            }
            return -1;
        });
        return max != null ? max : -1;
    }

    @Override
    public @Nullable Island getIslandByRegion(RegionCoordinate position) {
        if (position == null) return null;

        return SQLExecute.queryMap(
                databaseLoader,
                SELECT_ISLAND_BY_POSITION_VALID,
                List.of(position.x(), position.z()),
                rs -> {
                    try {
                        if (rs.next()) return constructIslandQuery(rs);
                    } catch (Exception e) {
                        logger.log(Level.ERROR, "getIslandByRegion failed", e);
                    }
                    return null;
                }
        );
    }

    @Override
    public boolean upsertCenterLocation(UUID islandId, Location location) {
        int affected = SQLExecute.update(databaseLoader, UPSERT_CENTER_LOCATION, List.of(
                islandId.toString(),
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ()
        ));
        return affected > 0;
    }

    @Override
    public List<Location> getCenterLocations(UUID islandId) {
        List<Location> result = SQLExecute.queryMap(databaseLoader, SELECT_CENTER_LOCATIONS,
                List.of(islandId.toString()), rs -> {
                    List<Location> locations = new ArrayList<>();
                    try {
                        while (rs.next()) {
                            String worldName = rs.getString("world_name");
                            double x = rs.getDouble("center_x");
                            double y = rs.getDouble("center_y");
                            double z = rs.getDouble("center_z");
                            org.bukkit.World world = Bukkit.getWorld(worldName);
                            if (world != null) {
                                locations.add(new Location(world, x, y, z));
                            } else {
                                logger.warn("World '{}' not found for island center {}", worldName, islandId);
                            }
                        }
                    } catch (SQLException e) {
                        logger.log(Level.ERROR, "getCenterLocations failed for island {}", islandId, e);
                    }
                    return locations;
                });
        return result != null ? result : List.of();
    }


    private Island constructIslandQuery(ResultSet rs) throws SQLException {
        String islandId = rs.getString("island_id");
        int maxMembers = rs.getInt("max_members");
        int regionX = rs.getInt("region_x");
        int regionZ = rs.getInt("region_z");
        double size = rs.getDouble("size");

        Timestamp timestamp = parseSqliteTimestamp(rs.getString("create_time"));

        RegionCoordinate position = new RegionCoordinate(regionX, regionZ);
        return new IslandHook(UUID.fromString(islandId), maxMembers, position, size, timestamp);
    }
}