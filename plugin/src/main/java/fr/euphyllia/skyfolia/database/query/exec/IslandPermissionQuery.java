package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandPermissionQuery {
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
    private final Logger logger = LogManager.getLogger(IslandPermissionQuery.class);
    private final InterneAPI api;
    private final String databaseName;

    public IslandPermissionQuery(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    public CompletableFuture<Boolean> updateIslandsPermission(UUID islandId, PermissionsType permissionsType, RoleType roleType, int permissions) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        MariaDBExecute.executeQueryDML(this.api, UPSERT_PERMISSIONS_ISLANDS.formatted(this.databaseName),
                List.of(islandId, permissionsType.name(), roleType.name(), permissions, roleType.name(), permissionsType.name(), permissions), i -> {
                    if (i != 0) {
                        completableFuture.complete(true);
                    } else {
                        completableFuture.complete(false);
                    }
                }, null);

        return completableFuture;
    }

    public CompletableFuture<PermissionRoleIsland> getIslandPermission(UUID islandId, PermissionsType permissionsType, RoleType roleType) {
        CompletableFuture<PermissionRoleIsland> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, ISLAND_PERMISSION_ROLE.formatted(this.databaseName), List.of(islandId, roleType.name(), permissionsType.name()), resultSet -> {
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
        return completableFuture;
    }
}
