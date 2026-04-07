package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.papi.SkylliaPAPIUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles {@code %skyllia_island_*%} placeholders.
 *
 * <table>
 *   <tr><th>Placeholder</th><th>Returns</th></tr>
 *   <tr><td>island_size</td><td>Island radius in blocks</td></tr>
 *   <tr><td>island_members_size</td><td>Current member count</td></tr>
 *   <tr><td>island_members_max_size</td><td>Maximum member slots</td></tr>
 *   <tr><td>island_role / island_rank</td><td>Player's role name (e.g. OWNER)</td></tr>
 *   <tr><td>island_role_value</td><td>Numeric value of the player's role</td></tr>
 * </table>
 */
public class IslandHandler implements PlaceholderHandler {

    @Override
    public @NotNull String prefix() {
        return "island";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @NotNull Island island,
                                   @NotNull String key) {
        RoleType role = SkylliaPAPIUtils.resolveRole(island, player.getUniqueId());

        return switch (key) {
            case "size" -> String.valueOf(island.getSize());
            case "members_size" -> String.valueOf(island.getMembers().size());
            case "members_max_size" -> String.valueOf(island.getMaxMembers());
            case "role", "rank" -> role.name();
            case "role_value" -> String.valueOf(role.getValue());
            default -> null;
        };
    }
}
