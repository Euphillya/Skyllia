package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.Island;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class IslandWarpQuery {

    private static final String SELECT_WARP_NAME = """
                SELECT iw.`world_name`, iw.`x`, iw.`y`, iw.`z`, iw.`pitch`, iw.`yaw`
                FROM `%s`.`islands_warp` iw
                INNER JOIN %s.islands i on i.island_id = iw.island_id
                WHERE iw.`island_id` = ? AND i.`disable` = 0 AND iw.`warp_name` = ?;
            """;
    private static final String UPSERT_WARPS = """
                INSERT INTO `%s`.`islands_warp`
                (island_id, warp_name, world_name, x, y, z, pitch, yaw)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                on DUPLICATE key UPDATE x = ?, y = ?, z = ?, pitch = ?, yaw = ?;
            """;
    private final Logger logger = LogManager.getLogger(IslandWarpQuery.class);
    private final InterneAPI api;
    private final String databaseName;

    public IslandWarpQuery(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<Boolean> updateWarp(Island island, String warpName, Location location) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api, UPSERT_WARPS.formatted(this.databaseName), List.of(
                    island.getIslandId(),
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
            ), i -> completableFuture.complete(i != 0), null);
        } catch (Exception ex) {
            logger.fatal("Error Disabled Island", ex);
            completableFuture.complete(false);
        }
        return completableFuture;
    }

    public CompletableFuture<@Nullable Location> getWarpByName(Island island, String warpName) {
        CompletableFuture<Location> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, SELECT_WARP_NAME.formatted(this.databaseName, this.databaseName), List.of(island.getIslandId(), warpName), resultSet -> {
            try {
                if (resultSet.next()) {
                    String worldName = resultSet.getString("world_name");
                    double locX = resultSet.getDouble("x");
                    double locY = resultSet.getDouble("y");
                    double locZ = resultSet.getDouble("z");
                    float locPitch = resultSet.getFloat("pitch");
                    float locYaw = resultSet.getFloat("yaw");

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        completableFuture.complete(null);
                        return;
                    }
                    completableFuture.complete(new Location(world, locX, locY, locZ, locYaw, locPitch));
                }
            } catch (SQLException e) {
                logger.log(Level.FATAL, e);
                completableFuture.complete(null);
            }
        }, null);
        return completableFuture;
    }


}
