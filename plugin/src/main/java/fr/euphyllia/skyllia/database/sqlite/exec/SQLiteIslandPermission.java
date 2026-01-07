package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.IslandPermissionQuery;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SQLiteIslandPermission extends IslandPermissionQuery {

    private static final Logger logger = LogManager.getLogger(SQLiteIslandPermission.class);

    private static final String UPSERT_PERMISSIONS_ISLANDS = """
            INSERT INTO islands_permissions (island_id, type, role, flags)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(island_id, type, role)
            DO UPDATE SET flags = excluded.flags;
            """;

    private static final String SELECT_ISLAND_PERMISSION = """
            SELECT flags
            FROM islands_permissions
            WHERE island_id = ? AND role = ? AND type = ?;
            """;

    private static final String UPSERT_GAMERULES_ISLANDS = """
            INSERT INTO islands_gamerule (island_id, flags)
            VALUES (?, ?)
            ON CONFLICT(island_id)
            DO UPDATE SET flags = excluded.flags;
            """;

    private static final String SELECT_ISLAND_GAMERULE = """
            SELECT flags
            FROM islands_gamerule
            WHERE island_id = ?;
            """;

    private final SQLiteDatabaseLoader databaseLoader;
    private final InterneAPI api;

    public SQLiteIslandPermission(InterneAPI api, SQLiteDatabaseLoader databaseLoader) {
        this.api = api;
        this.databaseLoader = databaseLoader;
    }



    @Override
    public Long getIslandGameRule(Island island) {
        CompletableFuture<Long> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_ISLAND_GAMERULE,
                    List.of(island.getId().toString()),
                    rs -> {
                        try {
                            if (rs.next()) {
                                future.complete(rs.getLong("flags"));
                            } else {
                                future.complete(0L);
                            }
                        } catch (SQLException ex) {
                            logger.error("getIslandGameRule", ex);
                            future.complete(0L);
                        }
                    },
                    null
            );
        } catch (DatabaseException e) {
            future.complete(0L);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> updateIslandGameRule(Island island, long value) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    UPSERT_GAMERULES_ISLANDS,
                    List.of(island.getId().toString(), value),
                    i -> future.complete(i > 0),
                    null
            );
        } catch (DatabaseException e) {
            future.complete(false);
        }
        return future;
    }
}
