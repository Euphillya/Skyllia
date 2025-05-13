package fr.euphyllia.skylliabank;

import fr.euphyllia.skylliabank.api.BankAccount;
import fr.euphyllia.skylliabank.api.BankGenerator;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BankManager {
    private final BankGenerator dbGenerator;

    public BankManager(BankGenerator dbGenerator) {
        this.dbGenerator = dbGenerator;
    }

    public CompletableFuture<BankAccount> getBankAccount(UUID islandId) {
        return dbGenerator.getBankAccount(islandId);
    }

    public CompletableFuture<Boolean> deposit(UUID islandId, double amount) {
        return dbGenerator.deposit(islandId, amount);
    }

    public CompletableFuture<Boolean> withdraw(UUID islandId, double amount) {
        return dbGenerator.withdraw(islandId, amount);
    }

    public CompletableFuture<Boolean> setBalance(UUID islandId, double balance) {
        return dbGenerator.setBalance(islandId, balance);
    }
}