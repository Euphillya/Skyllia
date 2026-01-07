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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MariaDBIslandWarp extends IslandWarpQuery {

    private static final String SELECT_WARP_NAME = """
                SELECT iw.`world_name`, iw.`x`, iw.`y`, iw.`z`, iw.`pitch`, iw.`yaw`
                FROM `%s`.`islands_warp` iw
                INNER JOIN %s.islands i on i.island_id = iw.island_id
                WHERE iw.`island_id` = ? AND i.`disable` = 0 AND iw.`warp_name` = ?;
            """;

    private static final String SELECT_LIST_WARP = """
                SELECT iw.`warp_name`, iw.`world_name`, iw.`x`, iw.`y`, iw.`z`, iw.`pitch`, iw.`yaw`
                FROM `%s`.`islands_warp` iw
                INNER JOIN %s.islands i on i.island_id = iw.island_id
                WHERE iw.`island_id` = ? AND i.`disable` = 0;
            """;
    private static final String UPSERT_WARPS = """
                INSERT INTO `%s`.`islands_warp`
                (island_id, warp_name, world_name, x, y, z, pitch, yaw)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                on DUPLICATE key UPDATE x = ?, y = ?, z = ?, pitch = ?, yaw = ?;
            """;

    private static final String DELETE_WARP = """
            DELETE FROM `%s`.`islands_warp`
            WHERE `island_id` = ? AND `warp_name` = ?;
            """;
    private static final Logger log = LoggerFactory.getLogger(MariaDBIslandWarp.class);

    public DatabaseLoader databaseLoader;
    public String databaseName;

    public MariaDBIslandWarp(DatabaseLoader databaseLoader, String databaseName) {
        this.databaseLoader = databaseLoader;
        this.databaseName = databaseName;
    }


    @Override
    public Boolean updateWarp(UUID islandId, String warpName, Location location) {
        AtomicBoolean result = new AtomicBoolean(false);
        SQLExecute.executeQueryDML(databaseLoader, UPSERT_WARPS.formatted(databaseName), List.of(
                islandId,
                warpName,
                location.getWorld().getName(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getPitch(),
                location.getYaw(),
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getPitch(),
                location.getYaw()
        ), affectedRows -> result.set(affectedRows != 0), null);
        return result.get();
    }

    @Override
    public @Nullable WarpIsland getWarpByName(UUID islandId, String warpName) {
        AtomicReference<WarpIsland> result = new AtomicReference<>();
        SQLExecute.executeQuery(databaseLoader, SELECT_WARP_NAME.formatted(databaseName, databaseName), List.of(islandId, warpName), resultSet -> {
            try {
                if (resultSet.next()) {
                    String worldName = resultSet.getString("world_name");
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");
                    float pitch = resultSet.getFloat("pitch");
                    float yaw = resultSet.getFloat("yaw");
                    World world = Bukkit.getWorld(worldName);
                    WarpIsland warpIsland = new WarpIsland(islandId, warpName, new Location(world, x, y, z, yaw, pitch));
                    result.set(warpIsland);
                }
            } catch (Exception e) {
                log.error("SQL Exception while fetching warp '{}' for island {}", warpName, islandId, e);
            }
        }, null);
        return result.get();
    }

    @Override
    public @Nullable List<WarpIsland> getListWarp(UUID islandId) {
        List<WarpIsland> result = new ArrayList<>();
        SQLExecute.executeQuery(databaseLoader, SELECT_LIST_WARP.formatted(databaseName, databaseName), List.of(islandId), resultSet -> {
            try {
                while (resultSet.next()) {
                    String warpName = resultSet.getString("warp_name");
                    String worldName = resultSet.getString("world_name");
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");
                    float pitch = resultSet.getFloat("pitch");
                    float yaw = resultSet.getFloat("yaw");
                    World world = Bukkit.getWorld(worldName);
                    WarpIsland warpIsland = new WarpIsland(islandId, warpName, new Location(world, x, y, z, yaw, pitch));
                    result.add(warpIsland);
                }
            } catch (Exception e) {
                log.error("SQL Exception while fetching warps for island {}", islandId, e);
            }
        }, null);
        return result;
    }

    @Override
    public Boolean deleteWarp(UUID islandId, String name) {
        AtomicBoolean result = new AtomicBoolean(false);
        SQLExecute.executeQueryDML(databaseLoader, DELETE_WARP.formatted(databaseName), List.of(
                islandId,
                name
        ), affectedRows -> result.set(affectedRows != 0), null);
        return result.get();
    }
}
