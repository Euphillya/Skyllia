package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.PermissionId;
import fr.euphyllia.skyllia.api.permissions.PermissionNode;
import fr.euphyllia.skyllia.api.permissions.PermissionRegistry;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.papi.SkylliaPAPIUtils;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * Handles {@code %skyllia_permissions_*%} placeholders.
 *
 * <p><b>Value placeholders</b> — return {@code "true"} or {@code "false"}:
 * <ul>
 *   <li>{@code permissions_<perm_key>} — checks against the requesting player's role.</li>
 *   <li>{@code permissions_role_<ROLE>_<perm_key>} — checks against the specified role
 *       regardless of who is requesting (useful for permission tables in GUIs).</li>
 * </ul>
 *
 * <p><b>Metadata placeholders</b> — return the translated display string:
 * <ul>
 *   <li>{@code permissions_name_<perm_key>} — display name of the permission.</li>
 *   <li>{@code permissions_desc_<perm_key>} — description of the permission.</li>
 * </ul>
 *
 * <p><b>Rendering strategy for metadata:</b>
 * <ul>
 *   <li>If {@code use_adventure_provided_replacer=true} in PlaceholderAPI's config,
 *       the raw MiniMessage string is returned directly — Adventure will render it.</li>
 *   <li>Otherwise, the Component is serialized to legacy {@code §}-codes so that
 *       plugins like DeluxeMenus can display colors correctly.</li>
 * </ul>
 */
public class PermissionsHandler implements PlaceholderHandler {

    private final boolean adventureReplacer;

    public PermissionsHandler() {
        this.adventureReplacer = PlaceholderAPIPlugin.getInstance()
                .getConfig()
                .getBoolean("use_adventure_provided_replacer", false);
    }

    private static @NotNull Locale resolveLocale(@NotNull OfflinePlayer player) {
        if (player.isOnline() && player.getPlayer() != null) {
            return player.getPlayer().locale();
        }
        return Locale.getDefault();
    }

    @Override
    public @NotNull String prefix() {
        return "permissions";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @NotNull Island island,
                                   @NotNull String key) {
        PermissionRegistry registry = SkylliaAPI.getPermissionRegistry();
        Locale locale = resolveLocale(player);

        if (key.startsWith("name_")) {
            return resolveNodeMeta(registry, key.substring("name_".length()), MetaType.NAME, locale);
        }
        if (key.startsWith("desc_")) {
            return resolveNodeMeta(registry, key.substring("desc_".length()), MetaType.DESCRIPTION, locale);
        }

        RoleType role;
        String permKey;

        if (key.startsWith("role_")) {
            // Format: role_<ROLE>_<perm_key>
            String withoutPrefix = key.substring("role_".length());
            role = null;
            permKey = null;
            for (RoleType candidate : RoleType.values()) {
                String candidateLower = candidate.name().toLowerCase(Locale.ROOT);
                if (withoutPrefix.startsWith(candidateLower + "_")) {
                    role = candidate;
                    permKey = withoutPrefix.substring(candidateLower.length() + 1);
                    break;
                }
            }
            if (role == null || permKey == null || permKey.isEmpty()) return null;
        } else {
            // Format: <perm_key>  →  use requesting player's role
            role = SkylliaPAPIUtils.resolveRole(island, player.getUniqueId());
            permKey = key;
        }

        NamespacedKey namespacedKey = SkylliaPAPIUtils.parseKeyLenient(permKey);
        if (namespacedKey == null) return null;

        PermissionId pid = registry.getIfPresent(namespacedKey);
        if (pid == null) return null;

        return String.valueOf(island.getCompiledPermissions().has(registry, role, pid));
    }

    /**
     * Resolves the translated string for a permission metadata field.
     * <p>
     * If the Adventure replacer is active in PAPI, returns the raw MiniMessage string
     * so Adventure can render it natively in the component pipeline.
     * Otherwise, serializes the rendered {@code Component} to legacy §-codes for
     * compatibility with plugins that only understand legacy formatting (e.g. DeluxeMenus).
     */
    private @Nullable String resolveNodeMeta(@NotNull PermissionRegistry registry,
                                             @NotNull String rawKey,
                                             @NotNull MetaType type,
                                             @NotNull Locale locale) {
        NamespacedKey namespacedKey = SkylliaPAPIUtils.parseKeyLenient(rawKey);
        if (namespacedKey == null) return "";

        PermissionId pid = registry.getIfPresent(namespacedKey);
        if (pid == null) return "";

        PermissionNode node = registry.node(pid);
        String langKey = switch (type) {
            case NAME -> node.displayName();
            case DESCRIPTION -> node.description();
        };

        if (adventureReplacer) {
            return ConfigLoader.language.translateRaw(locale, langKey, Map.of());
        } else {
            return LegacyComponentSerializer.legacySection()
                    .serialize(ConfigLoader.language.translate(locale, langKey, Map.of(), false));
        }
    }

    private enum MetaType {NAME, DESCRIPTION}
}