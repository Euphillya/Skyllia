package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.FlagNode;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.skyblock.Island;
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
 * Handles {@code %skyllia_flags_*%} placeholders.
 * Also registered under the legacy prefix {@code gamerule_}.
 *
 * <p><b>Value placeholders</b> — return {@code "true"} or {@code "false"}:
 * <ul>
 *   <li>{@code flags_<flag_key>} — current value of the flag on the island.</li>
 *   <li>{@code gamerule_<flag_key>} — same (legacy alias).</li>
 * </ul>
 *
 * <p><b>Metadata placeholders</b> — return the translated display string:
 * <ul>
 *   <li>{@code flags_name_<flag_key>} — display name of the flag.</li>
 *   <li>{@code flags_desc_<flag_key>} — description of the flag.</li>
 * </ul>
 *
 * <p>The flag key may be a full namespaced key ({@code skyllia:pvp})
 * or a bare name ({@code pvp}) — the {@code skyllia:} namespace is
 * prepended automatically as a fallback.
 *
 * <p><b>Rendering strategy for metadata:</b>
 * <ul>
 *   <li>If {@code use_adventure_provided_replacer=true} in PlaceholderAPI's config,
 *       the raw MiniMessage string is returned directly — Adventure will render it.</li>
 *   <li>Otherwise, the Component is serialized to legacy {@code §}-codes so that
 *       plugins like DeluxeMenus can display colors correctly.</li>
 * </ul>
 */
public class FlagsHandler implements PlaceholderHandler {

    private final boolean adventureReplacer;

    public FlagsHandler() {
        this.adventureReplacer = PlaceholderAPIPlugin.getInstance()
                .getConfig()
                .getBoolean("use_adventure_provided_replacer", false);
    }

    @Override
    public @NotNull String prefix() {
        return "flags";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @NotNull Island island,
                                   @NotNull String key) {
        IslandFlagRegistry registry = SkylliaAPI.getFlagRegistry();
        Locale locale = resolveLocale(player);

        if (key.startsWith("name_")) {
            return resolveNodeMeta(registry, key.substring("name_".length()), MetaType.NAME, locale);
        }
        if (key.startsWith("desc_")) {
            return resolveNodeMeta(registry, key.substring("desc_".length()), MetaType.DESCRIPTION, locale);
        }

        NamespacedKey namespacedKey = SkylliaPAPIUtils.parseKeyLenient(key);
        if (namespacedKey == null) return null;

        FlagId fid = registry.getIfPresent(namespacedKey);
        if (fid == null) return null;

        return String.valueOf(island.getIslandFlags().has(registry, fid));
    }

    private enum MetaType { NAME, DESCRIPTION }

    private static @NotNull Locale resolveLocale(@NotNull OfflinePlayer player) {
        if (player.isOnline() && player.getPlayer() != null) {
            return player.getPlayer().locale();
        }
        return Locale.getDefault();
    }

    /**
     * Resolves the translated string for a flag metadata field.
     * <p>
     * If the Adventure replacer is active in PAPI, returns the raw MiniMessage string
     * so Adventure can render it natively in the component pipeline.
     * Otherwise, serializes the rendered {@code Component} to legacy §-codes for
     * compatibility with plugins that only understand legacy formatting (e.g. DeluxeMenus).
     */
    private @Nullable String resolveNodeMeta(@NotNull IslandFlagRegistry registry,
                                             @NotNull String rawKey,
                                             @NotNull MetaType type,
                                             @NotNull Locale locale) {
        NamespacedKey namespacedKey = SkylliaPAPIUtils.parseKeyLenient(rawKey);
        if (namespacedKey == null) return "";

        FlagId fid = registry.getIfPresent(namespacedKey);
        if (fid == null) return "";

        FlagNode node = registry.node(fid);
        String langKey = switch (type) {
            case NAME        -> node.displayName();
            case DESCRIPTION -> node.description();
        };

        if (adventureReplacer) {
            return ConfigLoader.language.translateRaw(locale, langKey, Map.of());
        } else {
            return LegacyComponentSerializer.legacySection()
                    .serialize(ConfigLoader.language.translate(locale, langKey, Map.of(), false));
        }
    }
}
