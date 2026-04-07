package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skyllia.api.permissions.FlagId;
import fr.euphyllia.skyllia.api.permissions.IslandFlagRegistry;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.papi.SkylliaPAPIUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles {@code %skyllia_flags_*%} placeholders.
 * Also registered under the legacy prefix {@code gamerule_}.
 *
 * <table>
 *   <tr><th>Placeholder</th><th>Returns</th></tr>
 *   <tr><td>flags_&lt;flag_key&gt;</td><td>{@code true} / {@code false}</td></tr>
 *   <tr><td>gamerule_&lt;flag_key&gt;</td><td>Same (legacy alias)</td></tr>
 * </table>
 * <p>
 * The flag key may be a full namespaced key ({@code skyllia:pvp})
 * or a bare name ({@code pvp}) — the {@code skyllia:} namespace is
 * prepended automatically as a fallback.
 */
public class FlagsHandler implements PlaceholderHandler {

    @Override
    public @NotNull String prefix() {
        return "flags";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @NotNull Island island,
                                   @NotNull String key) {
        IslandFlagRegistry registry = SkylliaAPI.getFlagRegistry();

        NamespacedKey namespacedKey = SkylliaPAPIUtils.parseKeyLenient(key);
        if (namespacedKey == null) return null;

        FlagId fid = registry.getIfPresent(namespacedKey);
        if (fid == null) return null;

        return String.valueOf(island.getIslandFlags().has(registry, fid));
    }
}
