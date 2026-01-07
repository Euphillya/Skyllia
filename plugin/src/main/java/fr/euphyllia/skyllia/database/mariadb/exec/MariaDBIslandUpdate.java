package fr.euphyllia.skyllia.database.mariadb.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.IslandUpdateQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.sql.MariaDBExecute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MariaDBIslandUpdate extends IslandUpdateQuery {

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

    private static final String UPDATE_MEMBERS_ISLAND = """
                UPDATE `%s`.islands
                SET `max_members` = ?
                WHERE `island_id` = ?;
            """;

    private static final String UPDATE_SIZE_ISLAND = """
                UPDATE `%s`.islands
                SET `size` = ?
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

    private static final String SELECT_LOCKED_ISLAND = """
                SELECT `locked`
                FROM `%s`.islands
                WHERE `island_id` = ?;
            """;
    private static final String UPDATE_LOCKED_ISLAND = """
                UPDATE `%s`.islands
                SET `locked` = ?
                WHERE `island_id` = ?;
            """;


    private final Logger logger = LogManager.getLogger(MariaDBIslandUpdate.class);
    private final InterneAPI api;
    private final String databaseName;

    public MariaDBIslandUpdate(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<Boolean> updateDisable(Island island, boolean disable) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), UPDATE_DISABLE_ISLAND.formatted(this.databaseName), List.of(disable ? 1 : 0, island.getId()), i -> completableFuture.complete(i != 0), null);
        } catch (Exception ex) {
            logger.fatal("Error Disabled Island", ex);
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> updatePrivate(Island island, boolean privateIsland) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), UPDATE_PRIVATE_ISLAND.formatted(this.databaseName), List.of(privateIsland ? 1 : 0, island.getId()), i -> completableFuture.complete(i != 0), null);
        } catch (Exception ex) {
            logger.fatal("Error Change Private/Public Island", ex);
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> isDisabledIsland(Island island) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_STATUS_ISLAND.formatted(this.databaseName), List.of(island.getId()), resultSet -> {
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
        } catch (DatabaseException e) {
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> isPrivateIsland(Island island) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), SELECT_PRIVATE_ISLAND.formatted(this.databaseName), List.of(island.getId()), resultSet -> {
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
        } catch (DatabaseException e) {
            completableFuture.complete(null);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> setMaxMemberInIsland(Island island, int newValue) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), UPDATE_MEMBERS_ISLAND.formatted(this.databaseName), List.of(
                    newValue, island.getId()
            ), i -> completableFuture.complete(i != 0), null);
        } catch (Exception e) {
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    @Override
    public CompletableFuture<Boolean> setSizeIsland(Island island, double newValue) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), UPDATE_SIZE_ISLAND.formatted(this.databaseName), List.of(
                    newValue, island.getId()
            ), i -> completableFuture.complete(i != 0), null);
        } catch (Exception e) {
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> setLockedIsland(Island island, boolean locked) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(),
                    UPDATE_LOCKED_ISLAND.formatted(this.databaseName),
                    List.of(locked ? 1 : 0, island.getId()),
                    i -> future.complete(i != 0), null);
        } catch (Exception e) {
            logger.fatal("Error Locked Island", e);
            future.complete(false);
        }
        return future;
    }

    public CompletableFuture<Boolean> isLockedIsland(Island island) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(),
                    SELECT_LOCKED_ISLAND.formatted(this.databaseName),
                    List.of(island.getId()),
                    resultSet -> {
                        try {
                            if (resultSet.next()) {
                                future.complete(resultSet.getInt("locked") == 1);
                            } else {
                                future.complete(null);
                            }
                        } catch (Exception e) {
                            future.complete(null);
                        }
                    }, null);
        } catch (DatabaseException e) {
            future.complete(null);
        }
        return future;
    }


}
