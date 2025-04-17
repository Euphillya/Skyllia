package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.IslandUpdateQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SQLiteIslandUpdate extends IslandUpdateQuery {

    private static final Logger logger = LogManager.getLogger(SQLiteIslandUpdate.class);

    private static final String SELECT_STATUS_ISLAND = """
            SELECT disable
            FROM islands
            WHERE island_id = ?;
            """;
    private static final String UPDATE_DISABLE_ISLAND = """
            UPDATE islands
            SET disable = ?
            WHERE island_id = ?;
            """;
    private static final String UPDATE_MEMBERS_ISLAND = """
            UPDATE islands
            SET max_members = ?
            WHERE island_id = ?;
            """;
    private static final String UPDATE_SIZE_ISLAND = """
            UPDATE islands
            SET size = ?
            WHERE island_id = ?;
            """;

    private static final String SELECT_PRIVATE_ISLAND = """
            SELECT private
            FROM islands
            WHERE island_id = ?;
            """;
    private static final String UPDATE_PRIVATE_ISLAND = """
            UPDATE islands
            SET private = ?
            WHERE island_id = ?;
            """;

    private final InterneAPI api;
    private final SQLiteDatabaseLoader databaseLoader;

    public SQLiteIslandUpdate(InterneAPI api, SQLiteDatabaseLoader databaseLoader) {
        this.api = api;
        this.databaseLoader = databaseLoader;
    }

    @Override
    public CompletableFuture<Boolean> updateDisable(Island island, boolean disable) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    UPDATE_DISABLE_ISLAND,
                    List.of(disable ? 1 : 0, island.getId().toString()),
                    i -> future.complete(i > 0),
                    null
            );
        } catch (Exception ex) {
            logger.fatal("Error disable Island (SQLite)", ex);
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> updatePrivate(Island island, boolean privateIsland) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    UPDATE_PRIVATE_ISLAND,
                    List.of(privateIsland ? 1 : 0, island.getId().toString()),
                    i -> future.complete(i > 0),
                    null
            );
        } catch (Exception ex) {
            logger.fatal("Error changing Private/Public Island (SQLite)", ex);
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> isDisabledIsland(Island island) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_STATUS_ISLAND,
                    List.of(island.getId().toString()),
                    rs -> {
                        try {
                            if (rs.next()) {
                                future.complete(rs.getInt("disable") == 1);
                            } else {
                                future.complete(null);
                            }
                        } catch (Exception e) {
                            future.complete(null);
                        }
                    },
                    null
            );
        } catch (DatabaseException e) {
            future.complete(null);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> isPrivateIsland(Island island) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_PRIVATE_ISLAND,
                    List.of(island.getId().toString()),
                    rs -> {
                        try {
                            if (rs.next()) {
                                future.complete(rs.getInt("private") == 1);
                            } else {
                                future.complete(false);
                            }
                        } catch (Exception e) {
                            future.complete(false);
                        }
                    },
                    null
            );
        } catch (DatabaseException e) {
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> setMaxMemberInIsland(Island island, int newValue) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    UPDATE_MEMBERS_ISLAND,
                    List.of(newValue, island.getId().toString()),
                    i -> future.complete(i > 0),
                    null
            );
        } catch (Exception e) {
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> setSizeIsland(Island island, double newValue) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    UPDATE_SIZE_ISLAND,
                    List.of(newValue, island.getId().toString()),
                    i -> future.complete(i > 0),
                    null
            );
        } catch (Exception e) {
            future.complete(false);
        }
        return future;
    }
}
