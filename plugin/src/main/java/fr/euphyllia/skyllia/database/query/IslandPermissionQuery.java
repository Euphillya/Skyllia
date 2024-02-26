package fr.euphyllia.skyllia.database.query;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class IslandPermissionQuery {

    public abstract CompletableFuture<Boolean> updateIslandsPermission(Island island, PermissionsType permissionsType, RoleType roleType, long permissions);

    public abstract CompletableFuture<PermissionRoleIsland> getIslandPermission(UUID islandId, PermissionsType permissionsType, RoleType roleType);

    public abstract CompletableFuture<Long> getIslandGameRule(Island island);

    public abstract CompletableFuture<Boolean> updateIslandGameRule(Island island, long value);

}
