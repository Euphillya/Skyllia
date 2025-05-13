package fr.euphyllia.skyllia_papi.hook;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import fr.euphyllia.skyllia.api.skyblock.Island;
import fr.euphyllia.skylliabank.SkylliaBank;
import fr.euphyllia.skylliabank.api.BankAccount;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Handles placeholders related to the SkylliaBank addon.
 */
public class BankPlaceHolder {

    /**
     * Formatter for decimal numbers.
     */
    private static final DecimalFormat df = new DecimalFormat("0.00");

    /**
     * Cache that maps an island to its cached balance string.
     */
    private static final LoadingCache<Island, Optional<String>> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .refreshAfterWrite(3, TimeUnit.SECONDS)
            .build(new CacheLoader<>() {
                @Override
                public @NotNull Optional<String> load(@NotNull Island island) {
                    return Optional.of(getLiveBalance(island));
                }

                /**
                 * Handles the background refresh of the cached balance.
                 *
                 * @param island    the island
                 * @param oldValue  the old balance value
                 * @param executor  the executor for the asynchronous task
                 * @return a {@link CompletableFuture} containing the refreshed balance
                 */
                @Override
                public @NotNull CompletableFuture<Optional<String>> asyncReload(
                        @NotNull Island island,
                        @NotNull Optional<String> oldValue,
                        @NotNull Executor executor
                ) {
                    return CompletableFuture.supplyAsync(() -> Optional.of(getLiveBalance(island)), executor);
                }
            });

    /**
     * Retrieves the cached balance for the given island.
     *
     * @param island the island
     * @return the cached balance string
     */
    private static String getCachedBalance(Island island) {
        Optional<String> optional = cache.get(island);
        return optional.isPresent() ? cache.get(island).get() : "0.00";
    }

    /**
     * Retrieves the live balance for the given island.
     *
     * @param island the island
     * @return the live balance string
     */
    private static String getLiveBalance(Island island) {
        try {
            return fetchBalance(island);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return "0.00";
        }
    }

    /**
     * Processes bank-related placeholders.
     *
     * @param island      the player's island
     * @param playerId    the player's UUID
     * @param placeholder the placeholder to process
     * @return the placeholder value as a string
     */
    public static String processPlaceholder(Island island, UUID playerId, String placeholder) {
        String placeholderLower = placeholder.toLowerCase(Locale.ROOT);
        return switch (placeholderLower) {
            case "bank_live" -> getLiveBalance(island);
            case "bank_cached" -> getCachedBalance(island);
            default -> "Not Supported";
        };
    }

    /**
     * Fetches the bank balance for the given island.
     *
     * @param island the island
     * @return the formatted balance string
     * @throws ExecutionException   if the computation threw an exception
     * @throws InterruptedException if the current thread was interrupted
     * @throws TimeoutException     if the wait timed out
     */
    private static String fetchBalance(Island island) throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<BankAccount> bankAccountFuture = SkylliaBank.getBankManager().getBankAccount(island.getId());
        if (bankAccountFuture == null) return "0.00";
        BankAccount bankAccount = bankAccountFuture.get();
        if (bankAccount == null) return "0.00";
        return df.format(bankAccount.balance());
    }
}
