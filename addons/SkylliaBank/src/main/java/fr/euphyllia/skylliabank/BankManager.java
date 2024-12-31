package fr.euphyllia.skylliabank;

import fr.euphyllia.skylliabank.database.MariaDBBankGenerator;
import fr.euphyllia.skylliabank.database.MariaDBBankInit;
import fr.euphyllia.skylliabank.model.BankAccount;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BankManager {
    private final MariaDBBankGenerator dbGenerator;

    public BankManager() {
        this.dbGenerator = MariaDBBankInit.getMariaDbBankGenerator();
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