package fr.euphyllia.skyllia_papi;

import fr.euphyllia.skyllia.Skyllia;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI expansion for the Skyllia plugin.
 */
public class ExpansionSkyllia extends PlaceholderExpansion {

    /**
     * Returns the identifier of this expansion.
     *
     * @return the identifier string
     */
    @Override
    public @NotNull String getIdentifier() {
        return "skyllia";
    }

    /**
     * Returns the name of the required plugin.
     *
     * @return the required plugin name
     */
    @Override
    public String getRequiredPlugin() {
        return "Skyllia";
    }

    /**
     * Returns the author of this expansion.
     *
     * @return the author's name
     */
    @Override
    public @NotNull String getAuthor() {
        return Skyllia.getInstance().getPluginMeta().getAuthors().getFirst();
    }

    /**
     * Returns the version of this expansion.
     *
     * @return the version string
     */
    @Override
    public @NotNull String getVersion() {
        return Skyllia.getInstance().getPluginMeta().getVersion();
    }

    /**
     * Handles placeholder requests.
     *
     * @param player      the player requesting the placeholder
     * @param placeholder the placeholder string
     * @return the processed placeholder value
     */
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String placeholder) {
        if (player == null) return "";
        return PlaceholderProcessor.process(player.getUniqueId(), placeholder);
    }
}
