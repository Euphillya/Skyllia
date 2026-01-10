package fr.euphyllia.skylliabank;

import fr.euphyllia.skylliabank.api.BankAccount;
import fr.euphyllia.skylliabank.api.BankGenerator;

import java.util.UUID;

public class BankManager {

    private final BankGenerator dbGenerator;

    public BankManager(BankGenerator dbGenerator) {
        this.dbGenerator = dbGenerator;
    }

    public BankAccount getBankAccount(UUID islandId) {
        return dbGenerator.getBankAccount(islandId);
    }

    public Boolean deposit(UUID islandId, double amount) {
        boolean ok = dbGenerator.deposit(islandId, amount);
        if (ok) SkylliaBank.getInstance().getPapiCache().invalidate(islandId);
        return ok;
    }

    public Boolean withdraw(UUID islandId, double amount) {
        boolean ok = dbGenerator.withdraw(islandId, amount);
        if (ok) SkylliaBank.getInstance().getPapiCache().invalidate(islandId);
        return ok;

    }

    public Boolean setBalance(UUID islandId, double balance) {
        boolean ok = dbGenerator.setBalance(islandId, balance);
        if (ok) SkylliaBank.getInstance().getPapiCache().invalidate(islandId);
        return ok;
    }

    public BankAccount getOrLoadBankAccount(UUID islandId) {
        return getBankAccount(islandId);
    }
}
