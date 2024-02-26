package fr.euphyllia.skyllia.database.sqlite.exec;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.database.query.IslandPermissionQuery;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SqliteIslandPermission extends IslandPermissionQuery {
    @Override
    public CompletableFuture<Boolean> updateIslandsPermission(Island island, PermissionsType permissionsType, RoleType roleType, long permissions) {
        return null;
    }

    @Override
    public CompletableFuture<PermissionRoleIsland> getIslandPermission(UUID islandId, PermissionsType permissionsType, RoleType roleType) {
        return null;
    }

    @Override
    public CompletableFuture<Long> getIslandGameRule(Island island) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> updateIslandGameRule(Island island, long value) {
        return null;
    }
}
