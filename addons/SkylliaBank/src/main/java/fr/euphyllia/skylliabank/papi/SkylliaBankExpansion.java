package fr.euphyllia.skylliabank.papi;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliabank.SkylliaBank;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SkylliaBankExpansion extends PlaceholderExpansion {

    private static final Logger log = LoggerFactory.getLogger(SkylliaBankExpansion.class);

    @Override
    public @NotNull String getIdentifier() {
        return "skybank";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Euphyllia";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull List<String> getPlaceholders() {
        return List.of(
                "balance",
                "balance_formatted"
        );
    }


    @Override
    public @NotNull String getVersion() {
        return SkylliaBank.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.hasPlayedBefore()) return "";

        UUID playerId = player.getUniqueId();
        var island = SkylliaAPI.getIslandByPlayerId(playerId);
        if (island == null) return "";

        UUID islandId = island.getId();

        double balance = SkylliaBank.getInstance().getPapiCache().getBalanceOrDefaultAndRefresh(
                SkylliaBank.getInstance(),
                islandId,
                () -> SkylliaBank.getBankManager().getBankAccount(islandId).balance(), // DB load mais exécuté en async
                0.0
        );

        return switch (params.toLowerCase()) {
            case "balance" -> String.valueOf(balance);
            case "balance_formatted" -> format(balance);
            default -> null;
        };
    }

    private String format(double amount) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.##");
        df.setDecimalFormatSymbols(
                java.text.DecimalFormatSymbols.getInstance(Locale.FRANCE)
        );
        return df.format(amount);
    }
}