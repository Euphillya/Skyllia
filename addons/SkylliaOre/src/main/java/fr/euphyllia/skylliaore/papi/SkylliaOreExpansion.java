package fr.euphyllia.skylliaore.papi;

import fr.euphyllia.skylliaore.SkylliaOre;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SkylliaOreExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "skyore";
    }

    @Override
    public String getAuthor() {
        return "Euphyllia";
    }

    @Override
    public String getVersion() {
        return SkylliaOre.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null || !player.hasPlayedBefore()) return "";
        UUID playerId = player.getUniqueId();
        var island = fr.euphyllia.skyllia.api.SkylliaAPI.getCacheIslandByPlayerId(playerId);
        if (island == null) return "";
        var generator = SkylliaOre.getInstance().getOreGenerator().getGenIsland(island.getId())
                .getNow(SkylliaOre.getDefaultConfig().getDefaultGenerator());
        return switch (params.toLowerCase()) {
            case "name" -> generator.name();
            default -> null;
        };
    }
}
