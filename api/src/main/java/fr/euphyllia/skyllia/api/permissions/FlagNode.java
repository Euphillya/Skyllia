package fr.euphyllia.skyllia.api.permissions;

import org.bukkit.NamespacedKey;

public record FlagNode(
        NamespacedKey node,
        String displayName,
        String description,
        boolean defaultValue
) {
    public FlagNode(NamespacedKey node, String displayName, String description) {
        this(node, displayName, description, false);
    }
}
