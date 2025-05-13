package fr.euphyllia.skylliabank.api;

import java.util.UUID;

/**
 * Represents a bank account associated with an island.
 *
 * @param islandId the unique identifier of the island
 * @param balance  the current balance of the bank account
 */
public record BankAccount(UUID islandId, double balance) {
}
