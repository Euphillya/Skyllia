package fr.euphyllia.skyfolia.api.skyblock.model;

import java.util.UUID;

public record PermissionRoleIsland(UUID islandId, RoleType roleType, int permission) {
}