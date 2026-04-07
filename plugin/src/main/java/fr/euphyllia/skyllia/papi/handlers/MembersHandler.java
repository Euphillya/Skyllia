package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.Players;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.papi.SkylliaPAPIUtils;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handles {@code %skyllia_member_*%} placeholders.
 * Exposes the island's member list as indexed entries,
 * suitable for rendering dynamic GUIs in DeluxeMenus.
 *
 * <table>
 *   <tr><th>Placeholder</th><th>Returns</th></tr>
 *   <tr><td>member_has_&lt;N&gt;</td><td>{@code true} if index N exists, otherwise {@code false}</td></tr>
 *   <tr><td>member_name_&lt;N&gt;</td><td>Last known name of the member at index N</td></tr>
 *   <tr><td>member_uuid_&lt;N&gt;</td><td>UUID of the member at index N</td></tr>
 *   <tr><td>member_role_&lt;N&gt;</td><td>Role name of the member at index N</td></tr>
 * </table>
 * <p>
 * Indices are zero-based. Out-of-range name/uuid/role requests return an empty string.
 */
public class MembersHandler implements PlaceholderHandler {

    @Override
    public @NotNull String prefix() {
        return "member";
    }

    @Override
    public @Nullable String handle(@NotNull OfflinePlayer player,
                                   @NotNull Island island,
                                   @NotNull String key) {
        List<Players> members = island.getMembers();

        if (key.startsWith("has_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("has_".length()));
            if (index < 0) return null;
            return String.valueOf(index < members.size());
        }

        if (key.startsWith("name_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("name_".length()));
            if (index < 0 || index >= members.size()) return "";
            return members.get(index).getLastKnowName();
        }

        if (key.startsWith("uuid_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("uuid_".length()));
            if (index < 0 || index >= members.size()) return "";
            return members.get(index).getMojangId().toString();
        }

        if (key.startsWith("role_")) {
            int index = SkylliaPAPIUtils.parseIndex(key.substring("role_".length()));
            if (index < 0 || index >= members.size()) return "";
            RoleType role = members.get(index).getRoleType();
            return role != null ? role.name() : RoleType.MEMBER.name();
        }

        return null;
    }
}
