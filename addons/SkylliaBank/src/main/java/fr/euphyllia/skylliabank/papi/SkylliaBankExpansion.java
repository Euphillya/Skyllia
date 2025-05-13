package fr.euphyllia.skylliabank.papi;

import fr.euphyllia.skylliabank.SkylliaBank;
import fr.euphyllia.skylliabank.api.BankAccount;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkylliaBankExpansion extends PlaceholderExpansion {

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
    public List<String> getPlaceholders() {
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
        var island = fr.euphyllia.skyllia.api.SkylliaAPI.getCacheIslandByPlayerId(playerId);
        if (island == null) return "";

        CompletableFuture<BankAccount> future = SkylliaBank.getBankManager().getBankAccount(island.getId());
        BankAccount bankAccount = future.getNow(null); // async safe because called synchronously by PAPI

        if (bankAccount == null) return "-1";

        return switch (params.toLowerCase()) {
            case "balance" -> String.valueOf(bankAccount.balance());
            case "balance_formatted" ->
                    me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, String.format("%.2f", bankAccount.balance()));
            default -> null;
        };
    }
}