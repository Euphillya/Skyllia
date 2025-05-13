package fr.euphyllia.skylliabank.database.sqlite;

import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skylliabank.api.BankAccount;
import fr.euphyllia.skylliabank.api.BankGenerator;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SQLiteBankGenerator implements BankGenerator {

    private static final String SELECT_BALANCE = """
            SELECT balance FROM island_bank WHERE island_id = ?;
            """;

    private static final String UPSERT_BALANCE = """
            INSERT INTO island_bank (island_id, balance)
            VALUES (?, ?)
            ON CONFLICT(island_id) DO UPDATE SET balance = excluded.balance;
            """;

    @Override
    public CompletableFuture<BankAccount> getBankAccount(UUID islandId) {
        CompletableFuture<BankAccount> future = new CompletableFuture<>();
        try {
            SQLiteBankInit.getPool().executeQuery(SELECT_BALANCE, List.of(islandId.toString()), rs -> {
                try {
                    if (rs.next()) {
                        double balance = rs.getDouble("balance");
                        future.complete(new BankAccount(islandId, balance));
                    } else {
                        future.complete(new BankAccount(islandId, 0.0));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, null);
        } catch (DatabaseException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> setBalance(UUID islandId, double balance) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            SQLiteBankInit.getPool().executeUpdate(UPSERT_BALANCE, List.of(islandId.toString(), balance), rows -> future.complete(rows > 0), null);
        } catch (DatabaseException e) {
            future.completeExceptionally(e);
        }
        return future;
    }

    @Override
    public CompletableFuture<Boolean> deposit(UUID islandId, double amount) {
        return getBankAccount(islandId).thenCompose(account -> setBalance(islandId, account.balance() + amount));
    }

    @Override
    public CompletableFuture<Boolean> withdraw(UUID islandId, double amount) {
        return getBankAccount(islandId).thenCompose(account -> {
            if (account.balance() < amount) {
                return CompletableFuture.completedFuture(false);
            }
            return setBalance(islandId, account.balance() - amount);
        });
    }
}