package fr.euphyllia.skylliabank.papi;

import fr.euphyllia.skyllia.api.SkylliaAPI;
import fr.euphyllia.skylliabank.SkylliaBank;
import fr.euphyllia.skylliabank.api.BankAccount;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SkylliaBankExpansion extends PlaceholderExpansion {

    private static final Logger log = LoggerFactory.getLogger(SkylliaBankExpansion.class);
    private final Map<UUID, Number> cacheBalance = new java.util.concurrent.ConcurrentHashMap<>();

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

    public void init(Plugin plugin) {
        Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> {
            SkylliaAPI.getAllIslandsValid().thenAcceptAsync(islands -> {
                for (var island : islands) {
                    try {
                        CompletableFuture<BankAccount> future = SkylliaBank.getBankManager().getBankAccount(island.getId());
                        BankAccount bankAccount = future.get();
                        if (bankAccount == null) continue;
                        double balance = bankAccount.balance();
                        cacheBalance.put(island.getId(), balance);
                    } catch (Exception exception) {
                        log.error("Failed to update bank balance cache for island {}", island.getId(), exception);
                    }
                }
            });
        }, 1, 60, TimeUnit.SECONDS);
    }


    @Override
    public @NotNull String getVersion() {
        return SkylliaBank.getInstance().getPluginMeta().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null || !player.hasPlayedBefore()) return "";

        UUID playerId = player.getUniqueId();
        var island = SkylliaAPI.getCacheIslandByPlayerId(playerId);
        if (island == null) return "";

        Number balance = cacheBalance.get(island.getId());

        if (balance == null) return "-1";

        return switch (params.toLowerCase()) {
            case "balance" -> String.valueOf(balance.doubleValue());
            case "balance_formatted" -> format(balance.doubleValue());
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