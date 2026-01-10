package fr.euphyllia.skyllia.api.permissions;

import org.bukkit.NamespacedKey;

public record PermissionNode(
        NamespacedKey node,
        String displayName,
        String description,
        boolean defaultValue
) {
    public PermissionNode(NamespacedKey node, String displayName, String description) {
        this(node, displayName, description, false);
    }
}
