package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.api.skyblock.model.Position;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
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

    private static final String ISLAND_EXIST_COUNT = """
            SELECT COUNT(*) AS island_exist FROM `%s`.`islands`;
            """;

    private static final String ADD_ISLANDS = """
                INSERT INTO `%s`.`islands`
                (`island_type`, `island_id`, `uuid_owner`, `disable`, `region_x`, `region_z`, `private`, `create_time`)
                VALUES(?,?,?,?,?,?,?,?);
            """;

    public IslandQuery(InterneAPI api, String databaseName) {
        this.logger = LogManager.getLogger("fr.euphyllia.skyfolia.database.query.exec.IslandQuery");
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<@Nullable Island> getIslandByOwnerId(UUID playerId) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_ISLAND_BY_OWNER.formatted(this.databaseName), List.of(playerId.toString()), resultSet -> {
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

    public CompletableFuture<@Nullable UUID> insertIslands(Island futurIsland) {
        CompletableFuture<UUID> completableFuture = new CompletableFuture<>();
        try {
            UUID res = UUID.randomUUID();
            MariaDBExecute.executeQueryDML(this.api, ADD_ISLANDS.formatted(this.databaseName), List.of(
                    futurIsland.getIslandType(), futurIsland.getIslandId(), futurIsland.getOwnerId(), 0, futurIsland.getPosition().regionX(), futurIsland.getPosition().regionZ(), futurIsland.isPrivateIsland() ? 1 : 0, futurIsland.getCreateDate()
            ), i -> {
                if (i != 0) {
                    completableFuture.complete(res);
                } else {
                    completableFuture.complete(null);
                }
            }, null);
        } catch (Exception e) {
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Integer> getCountIslandExist() {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, ISLAND_EXIST_COUNT.formatted(this.databaseName), null, resultSet ->  {
            try {
                if (resultSet.next()) {
                    completableFuture.complete(resultSet.getInt("island_exist"));
                } else {
                    completableFuture.complete(-1);
                }
            } catch (SQLException e) {
                completableFuture.complete(-1);
            }
        }, null);
        return completableFuture;
    }

}
