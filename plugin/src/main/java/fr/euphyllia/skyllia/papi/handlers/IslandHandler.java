package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.papi.SkylliaPAPIUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Handles {@code %skyllia_island_*%} placeholders.
 *
 * <table>
 *   <caption>Available placeholders</caption>
 *   <tr><th>Placeholder</th><th>Returns</th></tr>
 * <p>
 *   <tr><td colspan="2"><b>— Identity —</b></td></tr>
 *   <tr><td>island_id</td><td>Island UUID</td></tr>
 *   <tr><td>island_owner_name</td><td>Owner's last known name</td></tr>
 *   <tr><td>island_owner_uuid</td><td>Owner's UUID</td></tr>
 *   <tr><td>island_create_date</td><td>Creation date (Timestamp.toString)</td></tr>
 * <p>
 *   <tr><td colspan="2"><b>— Size —</b></td></tr>
 *   <tr><td>island_size</td><td>Island radius in blocks (double)</td></tr>
 *   <tr><td>island_size_int</td><td>Island radius rounded to int</td></tr>
 *   <tr><td>island_members_size</td><td>Current member count</td></tr>
 *   <tr><td>island_members_max_size</td><td>Maximum member slots</td></tr>
 * <p>
 *   <tr><td colspan="2"><b>— Role —</b></td></tr>
 *   <tr><td>island_role / island_rank</td><td>Requesting player's role name (e.g. OWNER)</td></tr>
 *   <tr><td>island_role_value</td><td>Numeric value of the requesting player's role</td></tr>
 * <p>
 *   <tr><td colspan="2"><b>— State —</b></td></tr>
 *   <tr><td>island_access</td><td>{@code true} if private, {@code false} if public</td></tr>
 *   <tr><td>island_disabled</td><td>{@code true} if the island is disabled</td></tr>
 * </table>
 *
 * <p>Warp-related placeholders are handled by {@link WarpHandler}.
 */
public class IslandHandler implements PlaceholderHandler {

    private static @NotNull String ownerName(@NotNull Island island) {
        Players owner = island.getOwner();
        return owner != null ? owner.getLastKnowName() : "";
    }

    private static @NotNull String ownerUuid(@NotNull Island island) {
        Players owner = island.getOwner();
        return owner != null ? owner.getMojangId().toString() : "";
    }

    private static @NotNull String createDate(@NotNull Island island) {
        return island.getCreateDate() != null ? island.getCreateDate().toString() : "";
    }

    @Override
    public @NotNull String prefix() {
        return "island";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @NotNull Island island,
                                   @NotNull String key) {
        return switch (key) {

            case "id" -> island.getId().toString();
            case "owner_name" -> ownerName(island);
            case "owner_uuid" -> ownerUuid(island);
            case "create_date" -> createDate(island);

            case "size" -> String.valueOf(island.getSize());
            case "size_int" -> String.valueOf((int) island.getSize());
            case "members_size" -> String.valueOf(island.getMembers().size());
            case "members_max_size" -> String.valueOf(island.getMaxMembers());

            case "role", "rank" -> SkylliaPAPIUtils.resolveRole(island, player.getUniqueId()).name();
            case "role_value" -> String.valueOf(SkylliaPAPIUtils.resolveRole(island, player.getUniqueId()).getValue());

            case "access" -> String.valueOf(island.isPrivateIsland());
            case "disabled" -> String.valueOf(island.isDisable());

            default -> null;
        };
    }
}
