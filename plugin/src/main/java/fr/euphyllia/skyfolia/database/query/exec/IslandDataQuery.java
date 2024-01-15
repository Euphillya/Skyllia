package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
import fr.euphyllia.skyfolia.managers.skyblock.IslandHook;
import fr.euphyllia.skyfolia.utils.IslandUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandDataQuery {

    private static final String SELECT_ISLAND_BY_OWNER = """
            SELECT `island_type`, `island_id`, `uuid_owner`, `disable`, `region_x`, `region_z`, `private`, `size`, `create_time`
            FROM `%s`.`islands`
            WHERE `uuid_owner` = ? AND `disable` = 0;
            """;
    private static final String SELECT_ISLAND_BY_ISLAND_ID = """
            SELECT `island_type`, `island_id`, `uuid_owner`, `disable`, `region_x`, `region_z`, `private`, `size`, `create_time`
            FROM `%s`.`islands`
            WHERE `island_id` = ?;
            """;
    private static final String ADD_ISLANDS = """
                INSERT INTO `%s`.`islands`
                    SELECT ?, ?, ?, 0, S.region_x, S.region_z, ?, ?, current_timestamp()
                    FROM `%s`.`spiral` S
                    WHERE S.region_x NOT IN (SELECT region_x FROM `%s`.`islands` S2 WHERE S.region_x = S2.region_x AND S.region_z = S2.region_z AND S2.DISABLE = 0)
                        AND S.region_z NOT IN (SELECT region_z FROM `%s`.`islands` S2 WHERE S.region_x = S2.region_x AND S.region_z = S2.region_z AND S2.DISABLE = 0)
                    ORDER BY
                        S.id
                LIMIT 1;
            """;
    private final Logger logger = LogManager.getLogger(IslandDataQuery.class);

    private final InterneAPI api;
    private final String databaseName;

    public IslandDataQuery(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<@Nullable Island> getIslandByOwnerId(UUID playerId) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_ISLAND_BY_OWNER.formatted(this.databaseName), List.of(playerId), resultSet -> {
            try {
                if (resultSet.next()) {
                    completableFuture.complete(this.constructIslandQuery(resultSet));
                } else {
                    completableFuture.complete(null);
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, e);
                completableFuture.complete(null);
            }
        }, null);
        return completableFuture;
    }

    public CompletableFuture<Boolean> insertIslands(Island futurIsland) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api, ADD_ISLANDS.formatted(this.databaseName, this.databaseName, this.databaseName, this.databaseName), List.of(
                    futurIsland.getIslandType().name(), futurIsland.getId(), futurIsland.getOwnerId(), 1, futurIsland.getSize()
            ), i -> completableFuture.complete(i != 0), null);
        } catch (Exception e) {
            logger.log(Level.FATAL, e);
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_ISLAND_BY_ISLAND_ID.formatted(this.databaseName), List.of(islandId), resultSet -> {
            try {
                if (resultSet.next()) {
                    completableFuture.complete(this.constructIslandQuery(resultSet));
                } else {
                    completableFuture.complete(null);
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, e);
                completableFuture.complete(null);
            }
        }, null);
        return completableFuture;

    }

    private @Nullable Island constructIslandQuery(ResultSet resultSet) throws SQLException {
        String islandType = resultSet.getString("island_type");
        String islandId = resultSet.getString("island_id");
        String ownerId = resultSet.getString("uuid_owner");
        int regionX = resultSet.getInt("region_x");
        int regionZ = resultSet.getInt("region_z");
        int size = resultSet.getInt("size");
        Timestamp timestamp = resultSet.getTimestamp("create_time");
        Position position = new Position(regionX, regionZ);
        try {
            return new IslandHook(this.api.getPlugin(), IslandUtils.getIslandType(islandType), UUID.fromString(islandId), UUID.fromString(ownerId), position, size, timestamp);
        } catch (MaxIslandSizeExceedException maxIslandSizeExceedException) {
            logger.log(Level.FATAL, maxIslandSizeExceedException);
            return null;
        }
    }
}
