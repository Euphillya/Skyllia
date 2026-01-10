package fr.euphyllia.skylliabank.database.mariadb;

import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import fr.euphyllia.skylliabank.api.BankAccount;
import fr.euphyllia.skylliabank.api.BankGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class MariaDBBankGenerator implements BankGenerator {

    private static final Logger log = LogManager.getLogger(MariaDBBankGenerator.class);

    private static final String SELECT_BANK_BALANCE = """
            SELECT `balance`
            FROM `island_bank`
            WHERE `island_id` = ?;
            """;

    private static final String UPSERT_BANK_BALANCE = """
            INSERT INTO `island_bank` (`island_id`, `balance`)
            VALUES(?, ?)
            ON DUPLICATE KEY UPDATE `balance` = VALUES(`balance`);
            """;

    private static final String DEPOSIT = """
            INSERT INTO `island_bank` (`island_id`, `balance`)
            VALUES(?, ?)
            ON DUPLICATE KEY UPDATE `balance` = `balance` + VALUES(`balance`);
            """;

    private static final String WITHDRAW = """
            UPDATE `island_bank`
            SET `balance` = `balance` - ?
            WHERE `island_id` = ?
              AND `balance` >= ?;
            """;

    private final DatabaseLoader loader;

    public MariaDBBankGenerator(DatabaseLoader databaseLoader) {
        this.loader = databaseLoader;
    }

    @Override
    public BankAccount getBankAccount(UUID islandId) {
        return SQLExecute.queryMap(loader, SELECT_BANK_BALANCE, List.of(islandId.toString()), rs -> {
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
        int affected = SQLExecute.update(loader, UPSERT_BANK_BALANCE, List.of(islandId.toString(), balance));
        return affected > 0;
    }

    @Override
    public Boolean deposit(UUID islandId, double amount) {
        if (amount <= 0) return false;
        int affected = SQLExecute.update(loader, DEPOSIT, List.of(islandId.toString(), amount));
        return affected > 0;
    }

    @Override
    public Boolean withdraw(UUID islandId, double amount) {
        if (amount <= 0) return false;

        int affected = SQLExecute.update(loader, WITHDRAW, List.of(amount, islandId.toString(), amount));
        return affected > 0;
    }
}