package fr.euphyllia.skyllia.database.mariadb.exec;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.database.query.IslandPermissionQuery;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.execute.MariaDBExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MariaDBIslandPermission extends IslandPermissionQuery {
    private static final String UPSERT_PERMISSIONS_ISLANDS = """
            INSERT INTO `%s`.`islands_permissions`
            (`island_id`, `type`, `role`, `flags`)
            VALUES (?, ?, ?, ?)
            on DUPLICATE key UPDATE `role` = ?, `type` = ?, `flags` = ?;
            """;
    private static final String ISLAND_PERMISSION_ROLE = """
            SELECT `flags`
            FROM `%s`.`islands_permissions`
            WHERE `island_id` = ? AND `role` = ? AND `type` = ?;
            """;

    private static final String UPSERT_GAMERULES_ISLANDS = """
            INSERT INTO `%s`.`islands_gamerule`
            (`island_id`, `flags`)
            VALUES (?,?)
            on DUPLICATE KEY UPDATE `flags` = ?;
            """;

    private static final String ISLAND_GAMERULES_ROLE = """
            SELECT `flags`
            FROM `%s`.`islands_gamerule`
            WHERE `island_id` = ?;
            """;
    private final Logger logger = LogManager.getLogger(MariaDBIslandPermission.class);
    private final InterneAPI api;
    private final String databaseName;

    public MariaDBIslandPermission(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<Boolean> updateIslandsPermission(Island island, PermissionsType permissionsType, RoleType roleType, long permissions) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), UPSERT_PERMISSIONS_ISLANDS.formatted(this.databaseName),
                    List.of(island.getId(), permissionsType.name(), roleType.name(), permissions, roleType.name(), permissionsType.name(), permissions), i -> {
                        completableFuture.complete(i != 0);
                    }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(false);
        }

        return completableFuture;
    }

    public CompletableFuture<PermissionRoleIsland> getIslandPermission(UUID islandId, PermissionsType permissionsType, RoleType roleType) {
        CompletableFuture<PermissionRoleIsland> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), ISLAND_PERMISSION_ROLE.formatted(this.databaseName), List.of(islandId, roleType.name(), permissionsType.name()), resultSet -> {
                try {
                    if (resultSet.next()) {
                        long flags = resultSet.getLong("flags");
                        PermissionRoleIsland permissionsIsland = new PermissionRoleIsland(islandId, permissionsType, roleType, flags);
                        completableFuture.complete(permissionsIsland);
                    } else {
                        PermissionRoleIsland permissionsIsland = new PermissionRoleIsland(islandId, permissionsType, roleType, 0);
                        completableFuture.complete(permissionsIsland);
                    }
                } catch (Exception e) {
                    logger.log(Level.FATAL, e.getMessage(), e);
                    completableFuture.complete(new PermissionRoleIsland(islandId, permissionsType, roleType, 0));
                }
            }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(new PermissionRoleIsland(islandId, permissionsType, roleType, 0));
        }
        return completableFuture;
    }

    public CompletableFuture<Long> getIslandGameRule(Island island) {
        CompletableFuture<Long> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQuery(this.api.getDatabaseLoader(), ISLAND_GAMERULES_ROLE.formatted(this.databaseName), List.of(
                    island.getId()
            ), resultSet -> {
                try {
                    if (resultSet.next()) {
                        completableFuture.complete(resultSet.getLong("flags"));
                    } else {
                        completableFuture.complete(0L);
                    }
                } catch (SQLException exception) {
                    logger.log(Level.FATAL, exception.getMessage(), exception);
                    completableFuture.complete(0L);
                }
            }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(0L);
        }
        return completableFuture;
    }

    public CompletableFuture<Boolean> updateIslandGameRule(Island island, long value) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        try {
            MariaDBExecute.executeQueryDML(this.api.getDatabaseLoader(), UPSERT_GAMERULES_ISLANDS.formatted(this.databaseName), List.of(island.getId(), value, value), var1 -> {
                completableFuture.complete(var1 != 0);
            }, null);
        } catch (DatabaseException e) {
            completableFuture.complete(false);
        }
        return completableFuture;
    }
}
