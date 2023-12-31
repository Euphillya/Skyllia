package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IslandUpdateQuery {

    private static final String SELECT_STATUS_ISLAND = """
                SELECT `disable`
                FROM `%s`.islands
                WHERE `island_id` = ?;
            """;
    private static final String UPDATE_DISABLE_ISLAND = """
                UPDATE `%s`.islands
                SET `disable` = ?
                WHERE `island_id` = ?;
            """;

    private static final String SELECT_PRIVATE_ISLAND = """
                SELECT  `private`
                FROM `%s`.islands
                WHERE `island_id` = ?;
            """;
    private static final String UPDATE_PRIVATE_ISLAND = """
                UPDATE `%s`.islands
                SET `private` = ?
                WHERE `island_id` = ?;
            """;
    private final Logger logger = LogManager.getLogger(IslandUpdateQuery.class);
    private final InterneAPI api;
    private final String databaseName;

    public IslandUpdateQuery(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<Boolean> updateDisable(Island island) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api, UPDATE_DISABLE_ISLAND.formatted(this.databaseName), List.of(island.isDisable() ? 1 : 0, island.getId()), i -> completableFuture.complete(i != 0), null);
        } catch (Exception ex) {
            logger.fatal("Error Disabled Island", ex);
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> updatePrivate(Island island) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api, UPDATE_PRIVATE_ISLAND.formatted(this.databaseName), List.of(island.isPrivateIsland() ? 1 : 0, island.getId()), i -> completableFuture.complete(i != 0), null);
        } catch (Exception ex) {
            logger.fatal("Error Change Private/Public Island", ex);
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> isDisabledIsland(Island island) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_STATUS_ISLAND.formatted(this.databaseName), List.of(island.getId()), resultSet -> {
            try {
                if (resultSet.next()) {
                    completableFuture.complete(resultSet.getInt("disable") == 1);
                } else {
                    completableFuture.complete(null);
                }
            } catch (Exception e) {
                completableFuture.complete(null);
            }
        }, null);
        return completableFuture;
    }

    public CompletableFuture<Boolean> isPrivateIsland(Island island) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_PRIVATE_ISLAND.formatted(this.databaseName), List.of(island.getId()), resultSet -> {
            try {
                if (resultSet.next()) {
                    completableFuture.complete(resultSet.getInt("private") == 1);
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
