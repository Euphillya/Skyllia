package fr.euphyllia.skyllia.database.postgresql;

import fr.euphyllia.skyllia.api.database.IslandDataQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.managers.skyblock.IslandHook;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgreSQLIslandData extends IslandDataQuery {

    private static final String SELECT_ISLAND_BY_OWNER = """
            SELECT i.*
            FROM islands i
            JOIN members_in_islands mi ON i.island_id = mi.island_id
            WHERE mi.role = 'OWNER'
              AND mi.uuid_player = ?
              AND i.disable = FALSE;
            """;

    private static final String SELECT_ISLAND_BY_PLAYER_ID = """
            SELECT i.*
            FROM islands i
            JOIN members_in_islands mi ON i.island_id = mi.island_id
            WHERE mi.role NOT IN ('BAN', 'VISITOR')
              AND mi.uuid_player = ?
              AND i.disable = FALSE
            LIMIT 1;
            """;

    private static final String SELECT_ISLAND_BY_ISLAND_ID = """
            SELECT island_id, disable, region_x, region_z, private, locked, size, create_time, max_members
            FROM islands
            WHERE island_id = ?;
            """;

    private static final String ADD_ISLANDS = """
            INSERT INTO islands (island_id, disable, region_x, region_z, private, size, create_time, max_members, locked)
            SELECT
                ?, FALSE, s.region_x, s.region_z, ?, ?, NOW(), ?, FALSE
            FROM spiral s
            LEFT JOIN islands i
                   ON s.region_x = i.region_x
                  AND s.region_z = i.region_z
                  AND (i.locked = TRUE OR i.disable = FALSE)
            WHERE i.region_x IS NULL
            ORDER BY s.id
            LIMIT 1;
            """;

    private static final String SELECT_ALL_ISLANDS_VALID = """
            SELECT island_id, disable, region_x, region_z, private, locked, size, create_time, max_members
            FROM islands
            WHERE disable = FALSE;
            """;

    private static final String SELECT_ISLAND_BY_POSITION_VALID = """
            SELECT island_id, disable, region_x, region_z, private, locked, size, create_time, max_members
            FROM islands
            WHERE region_x = ?
              AND region_z = ?
              AND disable = FALSE
              AND locked = FALSE
            LIMIT 1;
            """;


    private static final Logger log = LoggerFactory.getLogger(PostgreSQLIslandData.class);

    private final DatabaseLoader databaseLoader;

    public PostgreSQLIslandData(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public @Nullable Island getIslandByOwnerId(UUID playerId) {
        return SQLExecute.queryMap(databaseLoader, SELECT_ISLAND_BY_OWNER, List.of(playerId), rs -> firstIsland(rs, playerId, "owner"));
    }

    @Override
    public @Nullable Island getIslandByPlayerId(UUID playerId) {
        return SQLExecute.queryMap(databaseLoader, SELECT_ISLAND_BY_PLAYER_ID, List.of(playerId), rs -> firstIsland(rs, playerId, "player"));
    }

    @Override
    public Boolean insertIslands(Island futurIsland) {
        int affected = SQLExecute.update(databaseLoader, ADD_ISLANDS, List.of(
                futurIsland.getId(),
                futurIsland.isPrivateIsland(),
                futurIsland.getSize(),
                futurIsland.getMaxMembers()
        ));
        return affected != 0;
    }

    @Override
    public @Nullable Island getIslandByIslandId(UUID islandId) {
        return SQLExecute.queryMap(databaseLoader, SELECT_ISLAND_BY_ISLAND_ID, List.of(islandId), rs -> {
            try {
                if (rs.next()) return constructIslandQuery(rs);
            } catch (SQLException e) {
                log.error("SQL Exception while fetching island by island id {}", islandId, e);
            }
            return null;
        });
    }

    @Override
    public List<Island> getAllIslandsValid() {
        List<Island> out = SQLExecute.queryMap(databaseLoader, SELECT_ALL_ISLANDS_VALID, null, rs -> {
            List<Island> islands = new ArrayList<>();
            try {
                while (rs.next()) {
                    Island island = constructIslandQuery(rs);
                    if (island != null) islands.add(island);
                }
            } catch (SQLException e) {
                log.error("SQL Exception while fetching all valid islands", e);
            }
            return islands;
        });
        return out != null ? out : List.of();
    }

    @Override
    public Integer getMaxMemberInIsland(Island island) {
        Integer max = SQLExecute.queryMap(databaseLoader, SELECT_ISLAND_BY_ISLAND_ID, List.of(island.getId()), rs -> {
            try {
                if (rs.next()) return rs.getInt("max_members");
            } catch (SQLException e) {
                log.error("SQL Exception while fetching max members for island id {}", island.getId(), e);
            }
            return -1;
        });
        return max != null ? max : -1;
    }

    @Override
    public @Nullable Island getIslandByPosition(Position position) {
        if (position == null) return null;

        return SQLExecute.queryMap(
                databaseLoader,
                SELECT_ISLAND_BY_POSITION_VALID,
                List.of(position.x(), position.z()),
                rs -> {
                    try {
                        if (rs.next()) return constructIslandQuery(rs);
                    } catch (SQLException e) {
                        log.error("SQL Exception while fetching island by position {} {}", position.x(), position.z(), e);
                    }
                    return null;
                }
        );
    }


    private @Nullable Island firstIsland(ResultSet rs, UUID playerId, String kind) {
        try {
            if (rs.next()) return constructIslandQuery(rs);
        } catch (SQLException e) {
            log.error("SQL Exception while fetching island by {} id {}", kind, playerId, e);
        }
        return null;
    }

    private @Nullable Island constructIslandQuery(ResultSet resultSet) throws SQLException {
        UUID islandId = (UUID) resultSet.getObject("island_id");
        int maxMembers = resultSet.getInt("max_members");
        int regionX = resultSet.getInt("region_x");
        int regionZ = resultSet.getInt("region_z");
        double size = resultSet.getDouble("size");
        Timestamp timestamp = resultSet.getTimestamp("create_time");

        Position position = new Position(regionX, regionZ);
        return new IslandHook(islandId, maxMembers, position, size, timestamp);
    }
}
