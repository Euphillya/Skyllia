package fr.euphyllia.skyllia.database.sqlite;

import fr.euphyllia.skyllia.api.database.IslandDataQuery;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.managers.skyblock.IslandHook;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
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
            Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(island));
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
            Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(island));
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

    private @Nullable Island constructIslandQuery(ResultSet rs) throws SQLException {
        String islandId = rs.getString("island_id");
        int maxMembers = rs.getInt("max_members");
        int regionX = rs.getInt("region_x");
        int regionZ = rs.getInt("region_z");
        double size = rs.getDouble("size");

        Timestamp timestamp = parseSqliteTimestamp(rs.getString("create_time"));

        Position position = new Position(regionX, regionZ);
        try {
            return new IslandHook(UUID.fromString(islandId), maxMembers, position, size, timestamp);
        } catch (MaxIslandSizeExceedException e) {
            logger.log(Level.ERROR, "Failed to construct island with id {} due to size exceed.", islandId, e);
            return null;
        }
    }
}
