package fr.euphyllia.skyllia.database.mariadb;

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

public class MariaDBIslandWarp extends IslandWarpQuery {

    private static final String SELECT_WARP_NAME = """
            SELECT iw.world_name, iw.x, iw.y, iw.z, iw.pitch, iw.yaw
            FROM islands_warp iw
            INNER JOIN islands i ON i.island_id = iw.island_id
            WHERE iw.island_id = ?
              AND i.disable = 0
              AND iw.warp_name = ?
            LIMIT 1;
            """;

    private static final String SELECT_LIST_WARP = """
            SELECT iw.warp_name, iw.world_name, iw.x, iw.y, iw.z, iw.pitch, iw.yaw
            FROM islands_warp iw
            INNER JOIN islands i ON i.island_id = iw.island_id
            WHERE iw.island_id = ?
              AND i.disable = 0;
            """;

    private static final String UPSERT_WARPS = """
            INSERT INTO islands_warp (island_id, warp_name, world_name, x, y, z, pitch, yaw)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                world_name = VALUES(world_name),
                x = VALUES(x),
                y = VALUES(y),
                z = VALUES(z),
                pitch = VALUES(pitch),
                yaw = VALUES(yaw);
            """;

    private static final String DELETE_WARP = """
            DELETE FROM islands_warp
            WHERE island_id = ? AND warp_name = ?;
            """;

    private static final Logger log = LoggerFactory.getLogger(MariaDBIslandWarp.class);

    private final DatabaseLoader databaseLoader;

    public MariaDBIslandWarp(DatabaseLoader databaseLoader) {
        this.databaseLoader = databaseLoader;
    }

    @Override
    public Boolean updateWarp(UUID islandId, String warpName, Location location) {
        if (location == null || location.getWorld() == null) return false;

        // Table = DOUBLE => conserve la précision (pas getBlockX/Y/Z)
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float pitch = location.getPitch();
        float yaw = location.getYaw();

        int affected = SQLExecute.update(databaseLoader, UPSERT_WARPS, List.of(
                islandId.toString(),
                warpName,
                location.getWorld().getName(),
                x, y, z,
                pitch, yaw
        ));

        return affected != 0;
    }

    @Override
    public @Nullable WarpIsland getWarpByName(UUID islandId, String warpName) {
        return SQLExecute.queryMap(databaseLoader, SELECT_WARP_NAME, List.of(islandId.toString(), warpName), rs -> {
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
        List<WarpIsland> out = SQLExecute.queryMap(databaseLoader, SELECT_LIST_WARP, List.of(islandId.toString()), rs -> {
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
        int affected = SQLExecute.update(databaseLoader, DELETE_WARP, List.of(islandId.toString(), name));
        return affected != 0;
    }
}
