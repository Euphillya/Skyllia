package fr.euphyllia.skylliabank.api;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a unified API for accessing and managing island bank accounts,
 * allowing for different database backends (e.g., MariaDB, SQLite).
 * <p>
 * Implementations of this interface are responsible for persisting and retrieving
 * island bank balances.
 */
public interface BankGenerator {

    /**
     * Retrieves the bank account associated with a specific island.
     *
     * @param islandId The UUID of the island.
     * @return A {@link CompletableFuture} containing the {@link BankAccount}.
     * If the account does not exist, it is usually returned with a balance of 0.
     */
    CompletableFuture<BankAccount> getBankAccount(UUID islandId);

    /**
     * Deposits a given amount of money into the specified island's bank account.
     *
     * @param islandId The UUID of the island.
     * @param amount   The amount to deposit (must be positive).
     * @return A {@link CompletableFuture} containing {@code true} if the operation succeeded, {@code false} otherwise.
     */
    CompletableFuture<Boolean> deposit(UUID islandId, double amount);

    /**
     * Withdraws a given amount of money from the specified island's bank account.
     * The withdrawal will fail if the island does not have enough balance.
     *
     * @param islandId The UUID of the island.
     * @param amount   The amount to withdraw (must be positive).
     * @return A {@link CompletableFuture} containing {@code true} if the operation succeeded, {@code false} otherwise.
     */
    CompletableFuture<Boolean> withdraw(UUID islandId, double amount);

    /**
     * Sets the exact balance of the specified island's bank account.
     * This will overwrite the existing balance.
     *
     * @param islandId The UUID of the island.
     * @param balance  The new balance to set (can be zero or positive).
     * @return A {@link CompletableFuture} containing {@code true} if the balance was updated successfully, {@code false} otherwise.
     */
    CompletableFuture<Boolean> setBalance(UUID islandId, double balance);
}