package fr.euphyllia.skyllia_papi;

import fr.euphyllia.skyllia.Skyllia;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Nullable
    @Override
    public String onRequest(final OfflinePlayer player, @NotNull final String placeholder) {
        return PlaceholderProcessor.process(player.getUniqueId(), placeholder);
    }
}
