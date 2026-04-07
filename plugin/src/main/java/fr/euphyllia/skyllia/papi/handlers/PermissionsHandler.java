package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.papi.SkylliaPAPIUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles {@code %skyllia_permissions_*%} placeholders.
 *
 * <p>Two forms are supported:
 * <ul>
 *   <li>{@code permissions_<perm_key>} — checks against the <em>requesting player's</em> role.</li>
 *   <li>{@code permissions_role_<ROLE>_<perm_key>} — checks against the specified role regardless
 *       of who is requesting (useful for displaying permission tables in GUIs).</li>
 * </ul>
 * <p>
 * Both return {@code "true"} or {@code "false"}.
 */
public class PermissionsHandler implements PlaceholderHandler {

    @Override
    public @NotNull String prefix() {
        return "permissions";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @NotNull Island island,
                                   @NotNull String key) {
        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();

        RoleType role;
        String permKey;

        if (key.startsWith("role_")) {
            // Format: role_<ROLE>_<perm_key>
            String withoutPrefix = key.substring("role_".length());
            int separatorIndex = withoutPrefix.indexOf('_');
            if (separatorIndex <= 0) return null;

            String roleName = withoutPrefix.substring(0, separatorIndex);
            permKey = withoutPrefix.substring(separatorIndex + 1);

            role = SkylliaPAPIUtils.parseRole(roleName);
            if (role == null) return null;
        } else {
            // Format: <perm_key>  →  use requesting player's role
            role = SkylliaPAPIUtils.resolveRole(island, player.getUniqueId());
            permKey = key;
        }

        NamespacedKey namespacedKey = SkylliaPAPIUtils.parseKeyLenient(permKey);
        if (namespacedKey == null) return null;

        PermissionId pid = registry.getIfPresent(namespacedKey);
        if (pid == null) return null;

        boolean allowed = island.getCompiledPermissions().has(registry, role, pid);
        return String.valueOf(allowed);
    }
}
