package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class IslandQuery {

    private InterneAPI api;
    private String databaseName;
    private final Logger logger;

    private static final String SELECT_ISLAND_BY_OWNER = """
            SELECT `island_type`, `island_id`, `uuid_owner`, `disable`, `region_x`, `region_z`, `private`, `create_time`
            FROM `%s`.`islands`
            WHERE `uuid_owner` = ? AND `disable` = 0;
            """;

    private static final String SELECT_ISLAND_BY_ISLAND_ID = """
            SELECT `island_type`, `island_id`, `uuid_owner`, `disable`, `region_x`, `region_z`, `private`, `create_time`
            FROM `%s`.`islands`
            WHERE `island_id` = ?;
            """;

    private static final String ADD_ISLANDS = """
                INSERT INTO `%s`.`islands`
                    SELECT ?, ?, ?, 0, S.region_x, S.region_z, ?, current_timestamp()
                    FROM `%s`.`spiral` S
                    WHERE S.region_x NOT IN (SELECT region_x FROM `%s`.`islands` S2 WHERE S.region_x = S2.region_x AND S.region_z = S2.region_z AND S2.DISABLE = 0)
                        AND S.region_z NOT IN (SELECT region_z FROM `%s`.`islands` S2 WHERE S.region_x = S2.region_x AND S.region_z = S2.region_z AND S2.DISABLE = 0)
                    ORDER BY
                        S.id
                LIMIT 1;
            """;

    public IslandQuery(InterneAPI api, String databaseName) {
        this.logger = LogManager.getLogger("fr.euphyllia.skyfolia.database.query.exec.IslandQuery");
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<@Nullable Island> getIslandByOwnerId(UUID playerId) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_ISLAND_BY_OWNER.formatted(this.databaseName), List.of(playerId), resultSet -> {
            try {
                if (resultSet.next()) {
                    String islandType = resultSet.getString("island_type");
                    String islandId = resultSet.getString("island_id");
                    int disable = resultSet.getInt("disable");
                    int regionX = resultSet.getInt("region_x");
                    int regionZ = resultSet.getInt("region_z");
                    int privateIsland = resultSet.getInt("private");
                    Timestamp timestamp = resultSet.getTimestamp("create_time");
                    Position position = new Position(regionX, regionZ);
                    Island island = new Island(islandType, UUID.fromString(islandId), playerId, disable, privateIsland, new CopyOnWriteArrayList<>(), new ConcurrentHashMap<>(), position, timestamp);
                    completableFuture.complete(island);
                } else {
                    completableFuture.complete(null);
                }
            } catch (Exception e) {
                completableFuture.complete(null);
            }
        }, null);
        return completableFuture;
    }

    public CompletableFuture<Boolean> insertIslands(Island futurIsland) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api, ADD_ISLANDS.formatted(this.databaseName, this.databaseName, this.databaseName, this.databaseName), List.of(
                    futurIsland.getIslandType(), futurIsland.getIslandId(), futurIsland.getOwnerId(), futurIsland.isPrivateIsland() ? 1 : 0
            ), i -> {
                completableFuture.complete(i != 0);
            }, null);
        } catch (Exception e) {
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_ISLAND_BY_ISLAND_ID.formatted(this.databaseName), List.of(islandId), resultSet -> {
            try {
                if (resultSet.next()) {
                    String islandType = resultSet.getString("island_type");
                    String ownerId = resultSet.getString("uuid_owner");
                    int disable = resultSet.getInt("disable");
                    int regionX = resultSet.getInt("region_x");
                    int regionZ = resultSet.getInt("region_z");
                    int privateIsland = resultSet.getInt("private");
                    Timestamp timestamp = resultSet.getTimestamp("create_time");
                    logger.log(Level.FATAL, regionX + " " + regionZ);
                    Position position = new Position(regionX, regionZ);
                    Island island = new Island(islandType, islandId, UUID.fromString(ownerId), disable, privateIsland, new CopyOnWriteArrayList<>(), new ConcurrentHashMap<>(), position, timestamp);
                    completableFuture.complete(island);
                } else {
                    completableFuture.complete(null);
                }
            } catch (Exception e) {
                completableFuture.complete(null);
            }
        }, null);
        return completableFuture;

    }

}
