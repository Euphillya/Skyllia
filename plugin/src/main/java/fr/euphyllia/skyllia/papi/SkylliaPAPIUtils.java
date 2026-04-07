package fr.euphyllia.skyllia.papi;

import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Shared utility methods for the Skyllia PlaceholderAPI expansion.
 */
public final class SkylliaPAPIUtils {

    private SkylliaPAPIUtils() {
    }

    /**
     * Resolves the {@link RoleType} of a player on a given island.
     * Returns {@link RoleType#VISITOR} if the player is not a member.
     */
    public static @NotNull RoleType resolveRole(@NotNull Island island, @NotNull UUID playerId) {
        var member = island.getMember(playerId);
        if (member == null) return RoleType.VISITOR;
        RoleType role = member.getRoleType();
        return role != null ? role : RoleType.VISITOR;
    }

    /**
     * Parses a role name (case-insensitive) into a {@link RoleType}.
     *
     * @return the matching role, or {@code null} if the input is invalid.
     */
    public static @Nullable RoleType parseRole(@Nullable String input) {
        if (input == null || input.isEmpty()) return null;
        try {
            return RoleType.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Parses a namespaced key leniently: tries the raw input first,
     * then prepends the {@code skyllia:} namespace as a fallback.
     *
     * @return a valid {@link NamespacedKey}, or {@code null} if the input is invalid.
     */
    public static @Nullable NamespacedKey parseKeyLenient(@Nullable String input) {
        if (input == null || input.isEmpty()) return null;
        NamespacedKey key = NamespacedKey.fromString(input);
        if (key != null) return key;
        return NamespacedKey.fromString("skyllia:" + input);
    }

    /**
     * Parses a non-negative integer index from a string.
     *
     * @return the parsed index, or {@code -1} if the input is invalid or negative.
     */
    public static int parseIndex(@Nullable String s) {
        if (s == null) return -1;
        try {
            int v = Integer.parseInt(s);
            return v >= 0 ? v : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
