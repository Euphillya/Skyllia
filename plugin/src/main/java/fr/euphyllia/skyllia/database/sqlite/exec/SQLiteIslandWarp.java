package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.IslandWarpQuery;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class SQLiteIslandWarp extends IslandWarpQuery {

    private static final Logger logger = LogManager.getLogger(SQLiteIslandWarp.class);

    private static final String UPSERT_WARPS = """
            INSERT INTO islands_warp (island_id, warp_name, world_name, x, y, z, pitch, yaw)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(island_id, warp_name)
            DO UPDATE SET world_name = excluded.world_name,
                          x = excluded.x,
                          y = excluded.y,
                          z = excluded.z,
                          pitch = excluded.pitch,
                          yaw = excluded.yaw;
            """;

    private static final String SELECT_WARP_NAME = """
            SELECT world_name, x, y, z, pitch, yaw
            FROM islands_warp iw
            JOIN islands i ON i.island_id = iw.island_id
            WHERE iw.island_id = ?
              AND i.disable = 0
              AND iw.warp_name = ?
            LIMIT 1;
            """;

    private static final String SELECT_LIST_WARP = """
            SELECT warp_name, world_name, x, y, z, pitch, yaw
            FROM islands_warp iw
            JOIN islands i ON i.island_id = iw.island_id
            WHERE iw.island_id = ?
              AND i.disable = 0;
            """;

    private static final String DELETE_WARP = """
            DELETE FROM islands_warp
            WHERE island_id = ? AND warp_name = ?;
            """;

    private final InterneAPI api;
    private final SQLiteDatabaseLoader databaseLoader;

    public SQLiteIslandWarp(InterneAPI api, SQLiteDatabaseLoader databaseLoader) {
        this.api = api;
        this.databaseLoader = databaseLoader;
    }

    @Override
    public CompletableFuture<Boolean> updateWarp(UUID islandId, String warpName, Location location) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    UPSERT_WARPS,
                    List.of(
                            islandId.toString(),
                            warpName,
                            location.getWorld().getName(),
                            location.getBlockX(),
                            location.getBlockY(),
                            location.getBlockZ(),
                            location.getPitch(),
                            location.getYaw()
                    ),
                    i -> future.complete(i > 0),
                    null
            );
        } catch (Exception e) {
            logger.error("updateWarp", e);
            future.complete(false);
        }
        return future;
    }

    @Override
    public CompletableFuture<@Nullable WarpIsland> getWarpByName(UUID islandId, String warpName) {
        CompletableFuture<WarpIsland> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_WARP_NAME,
                    List.of(islandId.toString(), warpName),
                    rs -> {
                        try {
                            if (rs.next()) {
                                String worldName = rs.getString("world_name");
                                double locX = rs.getDouble("x");
                                double locY = rs.getDouble("y");
                                double locZ = rs.getDouble("z");
                                float locPitch = rs.getFloat("pitch");
                                float locYaw = rs.getFloat("yaw");

                                World world = Bukkit.getWorld(worldName);
                                if (world == null) {
                                    future.complete(null);
                                    return;
                                }
                                WarpIsland warpIsland = new WarpIsland(
                                        islandId,
                                        warpName,
                                        new Location(world, locX, locY, locZ, locYaw, locPitch)
                                );
                                future.complete(warpIsland);
                            } else {
                                future.complete(null);
                            }
                        } catch (SQLException ex) {
                            logger.error("getWarpByName", ex);
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
    public CompletableFuture<@Nullable CopyOnWriteArrayList<WarpIsland>> getListWarp(UUID islandId) {
        CompletableFuture<CopyOnWriteArrayList<WarpIsland>> future = new CompletableFuture<>();
        try {
            databaseLoader.executeQuery(
                    SELECT_LIST_WARP,
                    List.of(islandId.toString()),
                    rs -> {
                        CopyOnWriteArrayList<WarpIsland> warpIslands = new CopyOnWriteArrayList<>();
                        try {
                            boolean any = false;
                            while (rs.next()) {
                                any = true;
                                String warpName = rs.getString("warp_name");
                                String worldName = rs.getString("world_name");
                                double locX = rs.getDouble("x");
                                double locY = rs.getDouble("y");
                                double locZ = rs.getDouble("z");
                                float locPitch = rs.getFloat("pitch");
                                float locYaw = rs.getFloat("yaw");
                                World world = Bukkit.getWorld(worldName);
                                if (world == null) {
                                    continue;
                                }
                                WarpIsland w = new WarpIsland(
                                        islandId,
                                        warpName,
                                        new Location(world, locX, locY, locZ, locYaw, locPitch)
                                );
                                warpIslands.add(w);
                            }
                            if (!any) {
                                future.complete(null);
                            } else {
                                future.complete(warpIslands);
                            }
                        } catch (SQLException ex) {
                            logger.error("getListWarp", ex);
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
    public CompletableFuture<Boolean> deleteWarp(UUID islandId, String name) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            databaseLoader.executeUpdate(
                    DELETE_WARP,
                    List.of(islandId.toString(), name),
                    affected -> future.complete(affected > 0),
                    null
            );
        } catch (Exception e) {
            logger.error("deleteWarp", e);
            future.complete(false);
        }
        return future;
    }
}
