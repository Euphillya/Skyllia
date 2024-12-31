package fr.euphyllia.skylliabank.model;

import java.util.UUID;

/**
 * Représente le compte bancaire d'une île Skyblock.
 */
public record BankAccount(UUID islandId, double balance) {
}
