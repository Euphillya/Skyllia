package fr.euphyllia.skyllia.database.mariadb.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.execute.MariaDBExecute;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.database.query.IslandDataQuery;
import fr.euphyllia.skyllia.managers.skyblock.IslandHook;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
                    SELECT ?, 0, S.region_x, S.region_z, ?, ?, current_timestamp(), ?
                    FROM `%s`.`spiral` S
                    WHERE S.region_x NOT IN (SELECT region_x FROM `%s`.`islands` S2 WHERE S.region_x = S2.region_x AND S.region_z = S2.region_z AND S2.DISABLE = 0)
                        AND S.region_z NOT IN (SELECT region_z FROM `%s`.`islands` S2 WHERE S.region_x = S2.region_x AND S.region_z = S2.region_z AND S2.DISABLE = 0)
                    ORDER BY
                        S.id
                LIMIT 1;
            """;
    private final Logger logger = LogManager.getLogger(MariaDBIslandData.class);

    private final InterneAPI api;
    private final String databaseName;

    public MariaDBIslandData(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<@Nullable Island> getIslandByOwnerId(UUID playerId) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_ISLAND_BY_OWNER.formatted(this.databaseName, this.databaseName), List.of(playerId), resultSet -> {
            try {
                if (resultSet.next()) {
                    Island island = this.constructIslandQuery(resultSet);
                    completableFuture.complete(island);
                    Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(island));
                } else {
                    completableFuture.complete(null);
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                completableFuture.complete(null);
            }
        }, null);
        return completableFuture;
    }

    public CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerId) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_ISLAND_BY_PLAYER_ID.formatted(this.databaseName, this.databaseName), List.of(playerId), resultSet -> {
            try {
                if (resultSet.next()) {
                    Island island = this.constructIslandQuery(resultSet);
                    completableFuture.complete(island);
                    Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(island));
                } else {
                    completableFuture.complete(null);
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                completableFuture.complete(null);
            }
        }, null);
        return completableFuture;
    }

    public CompletableFuture<Boolean> insertIslands(Island futurIsland) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), ADD_ISLANDS.formatted(this.databaseName, this.databaseName, this.databaseName, this.databaseName), List.of(
                    futurIsland.getId(), 1, futurIsland.getSize(), futurIsland.getMaxMembers()
            ), i -> completableFuture.complete(i != 0), null);
        } catch (Exception e) {
            logger.log(Level.FATAL, e.getMessage(), e);
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId) {
        CompletableFuture<Island> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_ISLAND_BY_ISLAND_ID.formatted(this.databaseName), List.of(islandId), resultSet -> {
            try {
                if (resultSet.next()) {
                    completableFuture.complete(this.constructIslandQuery(resultSet));
                } else {
                    completableFuture.complete(null);
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                completableFuture.complete(null);
            }
        }, null);
        return completableFuture;

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
            return new IslandHook(this.api.getPlugin(), UUID.fromString(islandId), maxMembers, position, size, timestamp);
        } catch (MaxIslandSizeExceedException maxIslandSizeExceedException) {
            logger.log(Level.FATAL, maxIslandSizeExceedException);
            return null;
        }
    }

    public CompletableFuture<Integer> getMaxMemberInIsland(Island island) {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_ISLAND_BY_ISLAND_ID.formatted(this.databaseName), List.of(island.getId()), resultSet -> {
            try {
                if (resultSet.next()) {
                    int maxMembers = resultSet.getInt("max_members");
                    completableFuture.complete(maxMembers);
                } else {
                    completableFuture.complete(-1);
                }
            } catch (SQLException e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                completableFuture.complete(-1);
            }
        }, null);
        return completableFuture;
    }
}
