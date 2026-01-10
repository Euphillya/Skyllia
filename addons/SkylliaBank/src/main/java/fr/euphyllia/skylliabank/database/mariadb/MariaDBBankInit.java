package fr.euphyllia.skylliabank.database.mariadb;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDB;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDBLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MariaDBBankInit extends DatabaseInitializeQuery {

    private static final Logger log = LoggerFactory.getLogger(MariaDBBankInit.class);
    private static final String CREATE_BANK_TABLE = """
            CREATE TABLE IF NOT EXISTS `island_bank` (
                `island_id` CHAR(36) NOT NULL,
                `balance` DOUBLE NOT NULL DEFAULT 0,
                PRIMARY KEY (`island_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static DatabaseLoader database;
    private static MariaDBBankGenerator mariaDbBankGenerator;

    public MariaDBBankInit() {
        initializeDatabase();
    }

    public static DatabaseLoader getPool() {
        return database;
    }

    public static MariaDBBankGenerator getMariaDbBankGenerator() {
        return mariaDbBankGenerator;
    }

    private void initializeDatabase() {
        MariaDB mariaDB = new MariaDB(ConfigLoader.database.getMariaDBConfig());
        database = new MariaDBLoader(mariaDB);
        mariaDbBankGenerator = new MariaDBBankGenerator(database);
    }

    @Override
    public Boolean init() {
        try {
            if (!database.loadDatabase()) {
                return false;
            }
            createBankTable();
        } catch (DatabaseException e) {
            log.error("Database initialization error: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    private void createBankTable() {
        try {
            SQLExecute.update(database, CREATE_BANK_TABLE, null);
        } catch (Exception exception) {
            log.error("Error creating island_bank table: {}", exception.getMessage(), exception);
        }
    }
}