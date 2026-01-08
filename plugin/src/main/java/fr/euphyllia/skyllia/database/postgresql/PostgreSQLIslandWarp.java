package fr.euphyllia.skyllia.database.postgresql;

import fr.euphyllia.skyllia.api.database.IslandWarpQuery;
import fr.euphyllia.skyllia.api.skyblock.model.WarpIsland;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostgreSQLIslandWarp extends IslandWarpQuery {

    private static final String UPSERT_WARPS = """
            INSERT INTO islands_warp (island_id, warp_name, world_name, x, y, z, pitch, yaw)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (island_id, warp_name)
            DO UPDATE SET
                world_name = EXCLUDED.world_name,
                x = EXCLUDED.x,
                y = EXCLUDED.y,
                z = EXCLUDED.z,
                pitch = EXCLUDED.pitch,
                yaw = EXCLUDED.yaw;
            """;

    private static final String SELECT_WARP_NAME = """
            SELECT iw.world_name, iw.x, iw.y, iw.z, iw.pitch, iw.yaw
            FROM islands_warp iw
            JOIN islands i ON i.island_id = iw.island_id
            WHERE iw.island_id = ?
              AND i.disable = FALSE
              AND iw.warp_name = ?
            LIMIT 1;
            """;

    private static final String SELECT_LIST_WARP = """
            SELECT iw.warp_name, iw.world_name, iw.x, iw.y, iw.z, iw.pitch, iw.yaw
            FROM islands_warp iw
            JOIN islands i ON i.island_id = iw.island_id
            WHERE iw.island_id = ?
              AND i.disable = FALSE;
            """;

    private static final String DELETE_WARP = """
            DELETE FROM islands_warp
            WHERE island_id = ? AND warp_name = ?;
            """;

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLIslandWarp.class);

    private final DatabaseLoader databaseLoader;

    public PostgreSQLIslandWarp(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public Boolean updateWarp(UUID islandId, String warpName, Location location) {
        if (location == null || location.getWorld() == null) return false;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();

        int affected = SQLExecute.update(databaseLoader, UPSERT_WARPS, List.of(
                islandId,
                warpName,
                location.getWorld().getName(),
                x, y, z, pitch, yaw
        ));
        return affected != 0;
    }

    @Override
    public @Nullable WarpIsland getWarpByName(UUID islandId, String warpName) {
        return SQLExecute.queryMap(databaseLoader, SELECT_WARP_NAME, List.of(islandId, warpName), rs -> {
            try {
                if (!rs.next()) return null;

                String worldName = rs.getString("world_name");
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                double z = rs.getDouble("z");
                float pitch = rs.getFloat("pitch");
                float yaw = rs.getFloat("yaw");

                World world = Bukkit.getWorld(worldName);
                if (world == null) return null;

                return new WarpIsland(islandId, warpName, new Location(world, x, y, z, yaw, pitch));
            } catch (Exception e) {
                log.error("SQL Exception while fetching warp '{}' for island {}", warpName, islandId, e);
                return null;
            }
        });
    }

    @Override
    public @Nullable List<WarpIsland> getListWarp(UUID islandId) {
        List<WarpIsland> out = SQLExecute.queryMap(databaseLoader, SELECT_LIST_WARP, List.of(islandId), rs -> {
            List<WarpIsland> result = new ArrayList<>();
            try {
                while (rs.next()) {
                    String warpName = rs.getString("warp_name");
                    String worldName = rs.getString("world_name");
                    double x = rs.getDouble("x");
                    double y = rs.getDouble("y");
                    double z = rs.getDouble("z");
                    float pitch = rs.getFloat("pitch");
                    float yaw = rs.getFloat("yaw");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;

                    result.add(new WarpIsland(islandId, warpName, new Location(world, x, y, z, yaw, pitch)));
                }
            } catch (Exception e) {
                log.error("SQL Exception while fetching warps for island {}", islandId, e);
            }
            return result;
        });

        return out != null ? out : List.of();
    }

    @Override
    public Boolean deleteWarp(UUID islandId, String name) {
        int affected = SQLExecute.update(databaseLoader, DELETE_WARP, List.of(islandId, name));
        return affected != 0;
    }
}
