package fr.euphyllia.skylliabank.database;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.mariadb.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDB;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.execute.MariaDBExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MariaDBBankInit extends DatabaseInitializeQuery {

    private static final Logger log = LoggerFactory.getLogger(MariaDBBankInit.class);
    private static final String CREATE_BANK_TABLE = """
            CREATE TABLE IF NOT EXISTS `%s`.`island_bank` (
                `island_id` CHAR(36) NOT NULL,
                `balance` DOUBLE NOT NULL DEFAULT 0,
                PRIMARY KEY (`island_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static DatabaseLoader database;
    private static MariaDBBankGenerator mariaDbBankGenerator;

    public MariaDBBankInit() {
        initializeDatabase();
        initializeGenerator();
    }

    public static DatabaseLoader getPool() {
        return database;
    }

    public static MariaDBBankGenerator getMariaDbBankGenerator() {
        return mariaDbBankGenerator;
    }

    private void initializeDatabase() {
        MariaDB mariaDB = new MariaDB(ConfigLoader.database.getMariaDBConfig());
        database = new DatabaseLoader(mariaDB);
    }

    private void initializeGenerator() {
        mariaDbBankGenerator = new MariaDBBankGenerator();
    }

    @Override
    public boolean init() throws DatabaseException {
        if (!database.loadDatabase()) {
            return false;
        }
        createBankTable();
        return true;
    }

    private void createBankTable() {
        try {
            MariaDBExecute.executeQuery(database,
                    CREATE_BANK_TABLE.formatted(ConfigLoader.database.getMariaDBConfig().database()));
        } catch (Exception exception) {
            log.error("Error creating island_bank table: {}", exception.getMessage(), exception);
        }
    }
}