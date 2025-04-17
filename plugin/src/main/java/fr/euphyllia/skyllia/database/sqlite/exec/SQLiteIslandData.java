package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.IslandDataQuery;
import fr.euphyllia.skyllia.api.event.SkyblockLoadEvent;
import fr.euphyllia.skyllia.api.exceptions.MaxIslandSizeExceedException;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.Position;
import fr.euphyllia.skyllia.managers.skyblock.IslandHook;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
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

public class SQLiteIslandData extends IslandDataQuery {

    private static final Logger logger = LogManager.getLogger(SQLiteIslandData.class);

    // On enlève le schéma "%s" + "." pour se contenter des noms de table
    private static final String SELECT_ISLAND_BY_OWNER = """
            SELECT i.*
            FROM islands i
            JOIN members_in_islands mi ON i.island_id = mi.island_id
            WHERE mi.role = 'OWNER'
            AND mi.uuid_player = ?
            AND i.disable = 0;
            """;

    private static final String SELECT_ISLAND_BY_PLAYER_ID = """
            SELECT i.*
            FROM islands i
            JOIN members_in_islands mi ON i.island_id = mi.island_id
            WHERE mi.role NOT IN ('BAN', 'VISITOR')
            AND mi.uuid_player = ?
            AND i.disable = 0
            LIMIT 1;
            """;

    private static final String SELECT_ISLAND_BY_ISLAND_ID = """
            SELECT island_id, disable, region_x, region_z, private, size, create_time, max_members
            FROM islands
            WHERE island_id = ?;
            """;

    private static final String ADD_ISLANDS = """
                INSERT INTO islands (
                    island_id, disable, region_x, region_z, private, size, create_time, max_members
                )
                SELECT
                    ?,                       -- 1) island_id
                    0,                       -- disable
                    s.region_x,
                    s.region_z,
                    ?,                       -- 2) private (0/1)
                    ?,                       -- 3) size (REAL)
                    DATETIME('now'),        -- create_time
                    ?                        -- 4) max_members
                FROM spiral s
                LEFT JOIN islands i
                    ON s.region_x = i.region_x
                   AND s.region_z = i.region_z
                   AND i.disable = 0
                WHERE i.region_x IS NULL
                ORDER BY s.id
                LIMIT 1;
            """;

    private final InterneAPI api;
    private final SQLiteDatabaseLoader databaseLoader;

    public SQLiteIslandData(InterneAPI api, SQLiteDatabaseLoader databaseLoader) {
        this.api = api;
        this.databaseLoader = databaseLoader;
    }

    @Override
    public CompletableFuture<@Nullable Island> getIslandByOwnerId(UUID playerId) {
        CompletableFuture<Island> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_ISLAND_BY_OWNER,
                    List.of(playerId.toString()),
                    rs -> {
                        try {
                            if (rs.next()) {
                                Island island = constructIslandQuery(rs);
                                future.complete(island);
                                // Événement
                                CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(island)));
                            } else {
                                future.complete(null);
                            }
                        } catch (Exception e) {
                            logger.log(Level.ERROR, "getIslandByOwnerId failed", e);
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
    public CompletableFuture<@Nullable Island> getIslandByPlayerId(UUID playerId) {
        CompletableFuture<Island> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_ISLAND_BY_PLAYER_ID,
                    List.of(playerId.toString()),
                    rs -> {
                        try {
                            if (rs.next()) {
                                Island island = constructIslandQuery(rs);
                                future.complete(island);
                                CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new SkyblockLoadEvent(island)));
                            } else {
                                future.complete(null);
                            }
                        } catch (Exception e) {
                            logger.log(Level.ERROR, "getIslandByPlayerId failed", e);
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
    public CompletableFuture<Boolean> insertIslands(Island futurIsland) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    ADD_ISLANDS,
                    List.of(
                            futurIsland.getId().toString(),
                            futurIsland.isPrivateIsland() ? 1 : 0,
                            futurIsland.getSize(),
                            futurIsland.getMaxMembers()
                    ),
                    affected -> future.complete(affected > 0),
                    null
            );

        } catch (Exception e) {
            logger.log(Level.ERROR, "insertIslands failed", e);
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<@Nullable Island> getIslandByIslandId(UUID islandId) {
        CompletableFuture<Island> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_ISLAND_BY_ISLAND_ID,
                    List.of(islandId.toString()),
                    rs -> {
                        try {
                            if (rs.next()) {
                                future.complete(constructIslandQuery(rs));
                            } else {
                                future.complete(null);
                            }
                        } catch (Exception e) {
                            logger.log(Level.ERROR, "getIslandByIslandId failed", e);
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

    private Island constructIslandQuery(ResultSet rs) throws SQLException, MaxIslandSizeExceedException {
        String islandId = rs.getString("island_id");
        int maxMembers = rs.getInt("max_members");
        int regionX = rs.getInt("region_x");
        int regionZ = rs.getInt("region_z");
        double size = rs.getDouble("size");
        String timeString = rs.getString("create_time");
        Timestamp timestamp = (timeString != null)
                ? Timestamp.valueOf(timeString.replace(" ", "T").replace("T", " ")) // à ajuster
                : null;
        Position position = new Position(regionX, regionZ);
        return new IslandHook(api.getPlugin(), UUID.fromString(islandId), maxMembers, position, size, timestamp);
    }

    @Override
    public CompletableFuture<Integer> getMaxMemberInIsland(Island island) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_ISLAND_BY_ISLAND_ID,
                    List.of(island.getId().toString()),
                    rs -> {
                        try {
                            if (rs.next()) {
                                future.complete(rs.getInt("max_members"));
                            } else {
                                future.complete(-1);
                            }
                        } catch (SQLException e) {
                            logger.log(Level.ERROR, "getMaxMemberInIsland failed", e);
                            future.complete(-1);
                        }
                    },
                    null
            );
        } catch (DatabaseException e) {
            future.complete(-1);
        }
        return future;
    }
}
