package fr.euphyllia.skyllia.papi.handlers;

import fr.euphyllia.skyllia.api.skyblock.Island;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contract for a single-responsibility placeholder handler.
 * <p>
 * Each implementation handles one prefix group (e.g. {@code island_},
 * {@code banned_}) and is registered in {@link fr.euphyllia.skyllia.papi.SkylliaExpansion}.
 * <p>
 * The {@code key} passed to {@link #handle} is the portion of the placeholder
 * that comes <em>after</em> the prefix has been stripped by the router.
 */
public interface PlaceholderHandler {

    /**
     * Returns the prefix this handler is responsible for (without trailing underscore).
     * <p>
     * Example: {@code "island"} matches placeholders starting with {@code island_}.
     */
    @NotNull String prefix();

    /**
     * Processes the placeholder and returns the resolved value.
     *
     * @param player the requesting player (may be offline)
     * @param island the island associated with the player (never {@code null})
     * @param key    the placeholder key with the prefix already stripped
     * @return the resolved string value, or {@code null} if the key is unrecognized
     */
    @Nullable String handle(@NotNull OfflinePlayer player, @NotNull Island island, @NotNull String key);
}
