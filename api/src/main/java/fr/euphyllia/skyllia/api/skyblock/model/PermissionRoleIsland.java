package fr.euphyllia.skyllia.api.skyblock.model;

import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsType;

import java.util.UUID;

public record PermissionRoleIsland(UUID islandId, PermissionsType permissionsType, RoleType roleType, long permission) {
}