package fr.euphyllia.skyfolia.api.skyblock.model;

import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsType;

import java.util.UUID;

public record PermissionRoleIsland(UUID islandId, PermissionsType permissionsType, RoleType roleType, long permission) {
}