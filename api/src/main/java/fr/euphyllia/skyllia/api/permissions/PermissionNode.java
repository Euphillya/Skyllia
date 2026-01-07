package fr.euphyllia.skyllia.api.permissions;

import org.bukkit.NamespacedKey;

public record PermissionNode(
        NamespacedKey node,
        String displayName,
        String description
) {
}
