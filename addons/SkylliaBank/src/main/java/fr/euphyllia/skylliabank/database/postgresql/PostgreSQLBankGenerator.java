package fr.euphyllia.skylliabank.database.postgresql;

import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import fr.euphyllia.skylliabank.api.BankAccount;
import fr.euphyllia.skylliabank.api.BankGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PostgreSQLBankGenerator implements BankGenerator {

    private static final Logger log = LogManager.getLogger(PostgreSQLBankGenerator.class);

    private static final String SELECT_BALANCE = """
            SELECT balance
            FROM island_bank
            WHERE island_id = ?;
            """;

    private static final String UPSERT_BALANCE = """
            INSERT INTO island_bank (island_id, balance)
            VALUES (?, ?)
            ON CONFLICT (island_id)
            DO UPDATE SET balance = EXCLUDED.balance;
            """;

    private static final String DEPOSIT = """
            INSERT INTO island_bank (island_id, balance)
            VALUES (?, ?)
            ON CONFLICT (island_id)
            DO UPDATE SET balance = island_bank.balance + EXCLUDED.balance;
            """;

    private static final String WITHDRAW = """
            UPDATE island_bank
            SET balance = balance - ?
            WHERE island_id = ?
              AND balance >= ?;
            """;

    private final DatabaseLoader loader;

    public PostgreSQLBankGenerator(DatabaseLoader loader) {
        this.loader = loader;
    }

    @Override
    public BankAccount getBankAccount(UUID islandId) {
        return SQLExecute.queryMap(loader, SELECT_BALANCE, List.of(islandId), rs -> {
            try {
                if (rs.next()) {
                    return new BankAccount(islandId, rs.getDouble("balance"));
                }
                return new BankAccount(islandId, 0.0);
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
                return new BankAccount(islandId, -1.0);
            }
        });
    }

    @Override
    public Boolean setBalance(UUID islandId, double balance) {
        int affected = SQLExecute.update(loader, UPSERT_BALANCE, List.of(islandId, balance));
        return affected > 0;
    }

    @Override
    public Boolean deposit(UUID islandId, double amount) {
        if (amount <= 0) return false;
        int affected = SQLExecute.update(loader, DEPOSIT, List.of(islandId, amount));
        return affected > 0;
    }

    @Override
    public Boolean withdraw(UUID islandId, double amount) {
        if (amount <= 0) return false;

        int affected = SQLExecute.update(loader, WITHDRAW, List.of(amount, islandId, amount));
        return affected > 0;
    }
}
