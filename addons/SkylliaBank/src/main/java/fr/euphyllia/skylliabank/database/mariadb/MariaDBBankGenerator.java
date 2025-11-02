package fr.euphyllia.skylliabank.database.mariadb;

import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.sql.execute.SQLExecute;
import fr.euphyllia.skylliabank.api.BankAccount;
import fr.euphyllia.skylliabank.api.BankGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MariaDBBankGenerator implements BankGenerator {

    private static final Logger log = LogManager.getLogger(MariaDBBankGenerator.class);
    private static final String SELECT_BANK_BALANCE = """
            SELECT `balance`
            FROM `%s`.`island_bank`
            WHERE `island_id` = ?;
            """;

    private static final String UPSERT_BANK_BALANCE = """
            INSERT INTO `%s`.`island_bank`
            (`island_id`, `balance`)
            VALUES(?, ?)
            ON DUPLICATE KEY UPDATE `balance` = VALUES(`balance`);
            """;

    private final String databaseName;

    public MariaDBBankGenerator() {
        this.databaseName = ConfigLoader.database.getMariaDBConfig().database();
    }

    public CompletableFuture<BankAccount> getBankAccount(UUID islandId) {
        CompletableFuture<BankAccount> future = new CompletableFuture<>();
        try {
            String query = SELECT_BANK_BALANCE.formatted(this.databaseName);
            SQLExecute.executeQuery(MariaDBBankInit.getPool(), query, List.of(islandId), resultSet -> {
                try {
                    if (resultSet.next()) {
                        double balance = resultSet.getDouble("balance");
                        BankAccount account = new BankAccount(islandId, balance);
                        future.complete(account);
                    } else {
                        BankAccount account = new BankAccount(islandId, 0.0);
                        future.complete(account);
                    }
                } catch (SQLException exception) {
                    log.error(exception.getMessage(), exception);
                    future.completeExceptionally(exception);
                }
            }, null);
        } catch (DatabaseException exception) {
            log.error(exception.getMessage(), exception);
            future.completeExceptionally(exception);
        }
        return future;
    }

    public CompletableFuture<Boolean> setBalance(UUID islandId, double balance) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        try {
            String query = UPSERT_BANK_BALANCE.formatted(this.databaseName);
            SQLExecute.executeQueryDML(MariaDBBankInit.getPool(), query, List.of(islandId, balance),
                    rowsAffected -> future.complete(rowsAffected > 0), null);
        } catch (DatabaseException exception) {
            log.error(exception.getMessage(), exception);
            future.complete(false);
        }
        return future;
    }

    public CompletableFuture<Boolean> deposit(UUID islandId, double amount) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        getBankAccount(islandId).thenCompose(account -> setBalance(islandId, account.balance() + amount))
                .thenAccept(future::complete)
                .exceptionally(ex -> {
                    log.error("Failed to deposit: {}", ex.getMessage(), ex);
                    future.complete(false);
                    return null;
                });
        return future;
    }

    public CompletableFuture<Boolean> withdraw(UUID islandId, double amount) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        getBankAccount(islandId).thenCompose(account -> {
            if (account.balance() < amount) {
                future.complete(false);
                return CompletableFuture.completedFuture(false);
            }
            return setBalance(islandId, account.balance() - amount);
        }).thenAccept(success -> {
            if (success) {
                future.complete(true);
            }
        }).exceptionally(ex -> {
            log.error("Failed to withdraw: {}", ex.getMessage(), ex);
            future.complete(false);
            return null;
        });
        return future;
    }
}