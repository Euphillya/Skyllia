package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.PermissionsIsland;
import fr.euphyllia.skyfolia.api.skyblock.model.RoleType;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class IslandPermissionQuery {
    private final Logger logger = LogManager.getLogger(IslandPermissionQuery.class);

    private final InterneAPI api;
    private final String databaseName;

    public IslandPermissionQuery(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
    }

    private static final String UPSERT_PERMISSIONS_ISLANDS = """
            INSERT INTO `%s`.`islands_permissions`
            (`island_id`, `role`, `flags`)
            VALUES (?, ?, ?)
            on DUPLICATE key UPDATE `role` = ?, `flags` = ?;
            """;
    private static final String ISLAND_PERMISSION_ROLE = """
            SELECT P.`flags` FROM `%s`.`islands_permissions` P
            WHERE P.`island_id` = ?
            AND P.`role` = ?;
            """;

    public CompletableFuture<Boolean> updateIslandsPermission(UUID islandId, RoleType roleType, int permissions) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        MariaDBExecute.executeQueryDML(this.api, UPSERT_PERMISSIONS_ISLANDS.formatted(this.databaseName),
                List.of(islandId, roleType.name(), permissions, roleType.name(), permissions), i -> {
                    if (i != 0) {
                        completableFuture.complete(true);
                    } else {
                        completableFuture.complete(false);
                    }
                }, null);

        return completableFuture;
    }

    public CompletableFuture<PermissionRoleIsland> getIslandPermission(UUID islandId, RoleType roleType) {
        CompletableFuture<PermissionRoleIsland> completableFuture = new CompletableFuture<>();
        MariaDBExecute.executeQuery(this.api, ISLAND_PERMISSION_ROLE.formatted(this.databaseName), List.of(islandId, roleType.name()), resultSet -> {
            try {
                if (resultSet.next()) {
                    int flags = resultSet.getInt("flags");
                    PermissionRoleIsland permissionsIsland = new PermissionRoleIsland(islandId, roleType, flags);
                    completableFuture.complete(permissionsIsland);
                } else {
                    PermissionRoleIsland permissionsIsland = new PermissionRoleIsland(islandId, roleType, 0);
                    completableFuture.complete(permissionsIsland);
                }
            } catch (Exception e) {
                logger.log(Level.FATAL, e.getMessage(), e);
                completableFuture.complete(new PermissionRoleIsland(islandId, roleType, 0));
            }
        }, null);
        return completableFuture;
    }
}
