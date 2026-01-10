package fr.euphyllia.skyllia.database.sqlite;

import fr.euphyllia.skyllia.api.database.IslandWarpQuery;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    private final DatabaseLoader databaseLoader;

    public SQLiteIslandWarp(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public Boolean updateWarp(UUID islandId, String warpName, Location location) {
        int affected = SQLExecute.update(databaseLoader, UPSERT_WARPS, List.of(
                islandId.toString(),
                warpName,
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getPitch(),
                location.getYaw()
        ));
        return affected > 0;
    }

    @Override
    public @Nullable WarpIsland getWarpByName(UUID islandId, String warpName) {
        return SQLExecute.queryMap(databaseLoader, SELECT_WARP_NAME, List.of(islandId.toString(), warpName), rs -> {
            try {
                if (!rs.next()) return null;

                String worldName = rs.getString("world_name");
                double locX = rs.getDouble("x");
                double locY = rs.getDouble("y");
                double locZ = rs.getDouble("z");
                float locPitch = rs.getFloat("pitch");
                float locYaw = rs.getFloat("yaw");

                World world = Bukkit.getWorld(worldName);
                if (world == null) return null;

                return new WarpIsland(
                        islandId,
                        warpName,
                        new Location(world, locX, locY, locZ, locYaw, locPitch)
                );
            } catch (SQLException ex) {
                logger.error("getWarpByName", ex);
                return null;
            }
        });
    }

    @Override
    public List<WarpIsland> getListWarp(UUID islandId) {
        List<WarpIsland> warps = SQLExecute.queryMap(databaseLoader, SELECT_LIST_WARP, List.of(islandId.toString()), rs -> {
            List<WarpIsland> out = new ArrayList<>();
            try {
                while (rs.next()) {
                    String warpName = rs.getString("warp_name");
                    String worldName = rs.getString("world_name");
                    double locX = rs.getDouble("x");
                    double locY = rs.getDouble("y");
                    double locZ = rs.getDouble("z");
                    float locPitch = rs.getFloat("pitch");
                    float locYaw = rs.getFloat("yaw");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;

                    out.add(new WarpIsland(
                            islandId,
                            warpName,
                            new Location(world, locX, locY, locZ, locYaw, locPitch)
                    ));
                }
            } catch (SQLException ex) {
                logger.error("getListWarp", ex);
            }
            return out;
        });

        return warps != null ? warps : List.of();
    }

    @Override
    public Boolean deleteWarp(UUID islandId, String name) {
        int affected = SQLExecute.update(databaseLoader, DELETE_WARP, List.of(
                islandId.toString(),
                name
        ));
        return affected > 0;
    }
}
