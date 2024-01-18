package fr.euphyllia.skyllia.cache;

import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PermissionRoleInIslandCache {
    private static final Logger logger = LogManager.getLogger(PermissionRoleInIslandCache.class);
    private static final ConcurrentHashMap<IslandRoleKey, PermissionRoleIsland> listPermission = new ConcurrentHashMap<>();

    public static void addPermissionInIsland(UUID islandId, RoleType roleType, PermissionsType permissionsType, PermissionRoleIsland permissionRoleIsland) {
        IslandRoleKey islandRoleKey = new IslandRoleKey(islandId, roleType, permissionsType);
        listPermission.put(islandRoleKey, permissionRoleIsland);
    }

    public static void deletePermissionInIsland(UUID islandId, RoleType roleType, PermissionsType permissionsType) {
        listPermission.remove(new IslandRoleKey(islandId, roleType, permissionsType));
    }

    public static PermissionRoleIsland getPermissionRoleIsland(UUID islandId, RoleType roleType, PermissionsType permissionsType) {
        IslandRoleKey islandRoleKey = new IslandRoleKey(islandId, roleType, permissionsType);
        return listPermission.getOrDefault(islandRoleKey, new PermissionRoleIsland(islandId, permissionsType, roleType, 0));

    }

    private record IslandRoleKey(UUID islandId, RoleType roleType, PermissionsType permissionsType) {
    }
}
