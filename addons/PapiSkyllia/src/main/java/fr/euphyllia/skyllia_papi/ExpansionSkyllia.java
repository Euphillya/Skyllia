package fr.euphyllia.skyllia_papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExpansionSkyllia extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "skyllia";
    }

    @Override
    public String getRequiredPlugin() {
        return "Skyllia";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Euphyllia";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.2-dev";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String placeholder) {
        if (player == null) return "";
        return PlaceholderProcessor.process(player.getUniqueId(), placeholder);
    }
}