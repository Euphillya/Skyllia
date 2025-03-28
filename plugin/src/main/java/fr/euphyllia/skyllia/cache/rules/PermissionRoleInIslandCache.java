package fr.euphyllia.skyllia.cache.rules;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.euphyllia.skyllia.api.skyblock.model.PermissionRoleIsland;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class PermissionRoleInIslandCache {

    private static SkyblockManager skyblockManager;
    private static final LoadingCache<IslandRoleKey, PermissionRoleIsland> PERMISSION_ROLE_CACHE =
            Caffeine.newBuilder()
                    .expireAfterAccess(15, TimeUnit.MINUTES)
                    .build(PermissionRoleInIslandCache::loadPermission);

    public static void init(SkyblockManager manager) {
        skyblockManager = manager;
    }

    public static PermissionRoleIsland getPermissionRoleIsland(UUID islandId, RoleType roleType, PermissionsType permissionsType) {
        return PERMISSION_ROLE_CACHE.get(new IslandRoleKey(islandId, roleType, permissionsType));
    }

    public static void invalidatePermission(UUID islandId, RoleType roleType, PermissionsType permissionsType) {
        PERMISSION_ROLE_CACHE.invalidate(new IslandRoleKey(islandId, roleType, permissionsType));
    }

    private static PermissionRoleIsland loadPermission(IslandRoleKey key) {
        return skyblockManager.getPermissionIsland(key.getIslandId(), key.getPermissionsType(), key.getRoleType()).join();
    }

    public static void invalidateIsland(UUID islandId) {
        for (RoleType roleType : RoleType.values()) {
            for (PermissionsType permissionsType : PermissionsType.values()) {
                invalidatePermission(islandId, roleType, permissionsType);
            }
        }
    }

    public static void invalidateAll() {
        PERMISSION_ROLE_CACHE.invalidateAll();
    }

    private static class IslandRoleKey {
        private final UUID islandId;
        private final RoleType roleType;
        private final PermissionsType permissionsType;

        public IslandRoleKey(UUID islandId, RoleType roleType, PermissionsType permissionsType) {
            this.islandId = islandId;
            this.roleType = roleType;
            this.permissionsType = permissionsType;
        }

        public UUID getIslandId() {
            return islandId;
        }

        public PermissionsType getPermissionsType() {
            return permissionsType;
        }

        public RoleType getRoleType() {
            return roleType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IslandRoleKey other)) return false;
            return islandId.equals(other.islandId)
                    && roleType == other.roleType
                    && permissionsType == other.permissionsType;
        }

        @Override
        public int hashCode() {
            return islandId.hashCode() ^ roleType.hashCode() ^ permissionsType.hashCode();
        }
    }
}