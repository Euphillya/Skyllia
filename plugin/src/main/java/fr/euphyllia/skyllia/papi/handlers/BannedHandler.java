package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.papi.SkylliaPAPIUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handles {@code %skyllia_banned_*%} placeholders.
 * Exposes the island's banned player list as indexed entries,
 * suitable for rendering dynamic GUIs in DeluxeMenus.
 *
 * <table>
 *   <tr><th>Placeholder</th><th>Returns</th></tr>
 *   <tr><td>banned_count</td><td>Total number of banned players</td></tr>
 *   <tr><td>banned_has_&lt;N&gt;</td><td>{@code true} if index N exists, otherwise {@code false}</td></tr>
 *   <tr><td>banned_name_&lt;N&gt;</td><td>Last known name of banned player at index N</td></tr>
 *   <tr><td>banned_uuid_&lt;N&gt;</td><td>UUID of banned player at index N</td></tr>
 * </table>
 * <p>
 * Indices are zero-based. Out-of-range name/uuid requests return an empty string.
 */
public class BannedHandler implements PlaceholderHandler {

    @Override
    public @NotNull String prefix() {
        return "banned";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @NotNull Island island,
                                   @NotNull String key) {
        List<Players> banned = island.getBannedMembers();

        if (key.equals("count")) {
            return String.valueOf(banned.size());
        }

        if (key.startsWith("has_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("has_".length()));
            if (index < 0) return null;
            return String.valueOf(index < banned.size());
        }

        if (key.startsWith("name_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("name_".length()));
            if (index < 0 || index >= banned.size()) return "";
            return banned.get(index).getLastKnowName();
        }

        if (key.startsWith("uuid_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("uuid_".length()));
            if (index < 0 || index >= banned.size()) return "";
            return banned.get(index).getMojangId().toString();
        }

        return null;
    }
}
