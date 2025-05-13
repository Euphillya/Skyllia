package fr.euphyllia.skylliabank;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skylliabank.api.BankAccount;
import fr.euphyllia.skylliabank.api.BankGenerator;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BankManager {

    private final BankGenerator dbGenerator;
    private final Cache<UUID, BankAccount> cache;

    public BankManager(BankGenerator dbGenerator) {
        this.dbGenerator = dbGenerator;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .maximumSize(ConfigLoader.general.getMaxIslands())
                .build();
    }

    public CompletableFuture<BankAccount> getBankAccount(UUID islandId) {
        BankAccount cached = cache.getIfPresent(islandId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return dbGenerator.getBankAccount(islandId).thenApply(account -> {
            cache.put(islandId, account);
            return account;
        });
    }

    public CompletableFuture<Boolean> deposit(UUID islandId, double amount) {
        return getBankAccount(islandId).thenCompose(account -> {
            return dbGenerator.deposit(islandId, amount).thenApply(success -> {
                if (success) {
                    cache.put(islandId, new BankAccount(islandId, account.balance() + amount));
                }
                return success;
            });
        });
    }

    public CompletableFuture<Boolean> withdraw(UUID islandId, double amount) {
        return getBankAccount(islandId).thenCompose(account -> {
            if (account.balance() < amount) return CompletableFuture.completedFuture(false);
            return dbGenerator.withdraw(islandId, amount).thenApply(success -> {
                if (success) {
                    cache.put(islandId, new BankAccount(islandId, account.balance() - amount));
                }
                return success;
            });
        });
    }

    public CompletableFuture<Boolean> setBalance(UUID islandId, double balance) {
        return dbGenerator.setBalance(islandId, balance).thenApply(success -> {
            if (success) {
                cache.put(islandId, new BankAccount(islandId, balance));
            }
            return success;
        });
    }

    public CompletableFuture<BankAccount> getOrLoadBankAccount(UUID islandId) {
        BankAccount cached = cache.getIfPresent(islandId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return getBankAccount(islandId);
    }


    public void invalidate(UUID islandId) {
        cache.invalidate(islandId);
    }
}
