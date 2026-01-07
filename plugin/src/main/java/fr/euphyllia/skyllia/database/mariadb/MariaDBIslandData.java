package fr.euphyllia.skyllia.database.mariadb;

import fr.euphyllia.skyllia.api.database.IslandDataQuery;
import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
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
import java.util.concurrent.atomic.AtomicReference;

public class MariaDBIslandData extends IslandDataQuery {

    private static final String SELECT_ISLAND_BY_OWNER = """
            SELECT i.*
            FROM `%s`.`islands` i
            JOIN `%s`.`members_in_islands` mi ON i.`island_id` = mi.`island_id`
            WHERE mi.`role` = 'OWNER'
            AND mi.`uuid_player` = ?
            AND i.`disable` = 0;
            """;

    private static final String SELECT_ISLAND_BY_PLAYER_ID = """
            SELECT i.*
            FROM `%s`.`islands` i
            JOIN `%s`.`members_in_islands` mi ON i.`island_id` = mi.`island_id`
            WHERE mi.`role` NOT IN ('BAN', 'VISITOR')
            AND mi.`uuid_player` = ?
            AND i.`disable` = 0 LIMIT 1;
            """;
    private static final String SELECT_ISLAND_BY_ISLAND_ID = """
            SELECT `island_id`, `disable`, `region_x`, `region_z`, `private`, `size`, `create_time`, `max_members`
            FROM `%s`.`islands`
            WHERE `island_id` = ?;
            """;
    private static final String ADD_ISLANDS = """
                INSERT INTO `%s`.`islands`
                (`island_id`,`disable`,`region_x`,`region_z`,`private`,`size`,`create_time`,`max_members`,`locked`)
                    SELECT ?, 0, S.region_x, S.region_z, ?, ?, CURRENT_TIMESTAMP(), ?, 0
                    FROM `%s`.`spiral` S
                    LEFT JOIN `%s`.`islands` S2
                    ON S.region_x = S2.region_x
                    AND S.region_z = S2.region_z
                AND (S2.locked = 1 OR S2.disable = 0)
                    WHERE S2.region_x IS NULL
                    ORDER BY S.id
                LIMIT 1;
            """;
    private static final String SELECT_ALL_ISLANDS_VALID = """
            SELECT `island_id`, `disable`, `region_x`, `region_z`, `private`, `size`, `create_time`, `max_members`
            FROM `%s`.`islands`
            WHERE `disable` = 0;
            """;

    private static final Logger log = LoggerFactory.getLogger(MariaDBIslandData.class);

    public DatabaseLoader databaseLoader;
    public String databaseName;

    public MariaDBIslandData(DatabaseLoader databaseLoader, String databaseName) {
        this.databaseLoader = databaseLoader;
        this.databaseName = databaseName;
    }

    @Override
    public @Nullable Island getIslandByOwnerId(UUID playerId) {
        AtomicReference<Island> island = new AtomicReference<>();
        SQLExecute.executeQuery(databaseLoader, SELECT_ISLAND_BY_OWNER.formatted(databaseName, databaseName), List.of(playerId), resultSet -> {
            try {
                if (resultSet.next()) {
                    island.set(constructIslandQuery(resultSet));
                }
            } catch (SQLException e) {
                log.error("SQL Exception while fetching island by owner id {}", playerId, e);
            }
        }, null);
        return island.get();
    }

    @Override
    public @Nullable Island getIslandByPlayerId(UUID playerId) {
        AtomicReference<Island> island = new AtomicReference<>();
        SQLExecute.executeQuery(databaseLoader, SELECT_ISLAND_BY_PLAYER_ID.formatted(databaseName, databaseName), List.of(playerId), resultSet -> {
            try {
                if (resultSet.next()) {
                    island.set(constructIslandQuery(resultSet));
                }
            } catch (SQLException e) {
                log.error("SQL Exception while fetching island by player id {}", playerId, e);
            }
        }, null);
        return island.get();
    }

    @Override
    public Boolean insertIslands(Island futurIsland) {
        AtomicReference<Boolean> result = new AtomicReference<>(false);
        SQLExecute.executeQueryDML(databaseLoader, ADD_ISLANDS.formatted(databaseName, databaseName, databaseName), List.of(
                futurIsland.getId(), 1, futurIsland.getSize(), futurIsland.getMaxMembers()
        ), i -> result.set(i != 0), null);
        return result.get();
    }

    @Override
    public @Nullable Island getIslandByIslandId(UUID islandId) {
        AtomicReference<Island> island = new AtomicReference<>();
        SQLExecute.executeQuery(databaseLoader, SELECT_ISLAND_BY_ISLAND_ID.formatted(databaseName), List.of(islandId), resultSet -> {
            try {
                if (resultSet.next()) {
                    island.set(constructIslandQuery(resultSet));
                }
            } catch (SQLException e) {
                log.error("SQL Exception while fetching island by island id {}", islandId, e);
            }
        }, null);
        return island.get();
    }

    @Override
    public List<Island> getAllIslandsValid() {
        List<Island> islands = new ArrayList<>();
        SQLExecute.executeQuery(databaseLoader, SELECT_ALL_ISLANDS_VALID.formatted(databaseName), null, resultSet -> {
            try {
                while (resultSet.next()) {
                    Island island = constructIslandQuery(resultSet);
                    if (island != null) {
                        islands.add(island);
                    }
                }
            } catch (SQLException e) {
                log.error("SQL Exception while fetching all valid islands", e);
            }
        }, null);
        return islands;
    }

    @Override
    public Integer getMaxMemberInIsland(Island island) {
        AtomicReference<Integer> maxMembers = new AtomicReference<>(-1);
        SQLExecute.executeQuery(databaseLoader, SELECT_ISLAND_BY_ISLAND_ID.formatted(databaseName), List.of(island.getId()), resultSet -> {
            try {
                if (resultSet.next()) {
                    int max = resultSet.getInt("max_members");
                    maxMembers.set(max);
                }
            } catch (SQLException e) {
                log.error("SQL Exception while fetching max members for island id {}", island.getId(), e);
            }
        }, null);
        return maxMembers.get();
    }

    private @Nullable Island constructIslandQuery(ResultSet resultSet) throws SQLException {
        String islandId = resultSet.getString("island_id");
        int maxMembers = resultSet.getInt("max_members");
        int regionX = resultSet.getInt("region_x");
        int regionZ = resultSet.getInt("region_z");
        double size = resultSet.getDouble("size");
        Timestamp timestamp = resultSet.getTimestamp("create_time");
        Position position = new Position(regionX, regionZ);
        try {
            return new IslandHook(UUID.fromString(islandId), maxMembers, position, size, timestamp);
        } catch (MaxIslandSizeExceedException maxIslandSizeExceedException) {
            log.error("Failed to construct island with id {} due to size exceed exception.", islandId, maxIslandSizeExceedException);
            return null;
        }
    }
}
