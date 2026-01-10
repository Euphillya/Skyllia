package fr.euphyllia.skylliabank.database.postgresql;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.postgre.Postgres;
import fr.euphyllia.skyllia.sgbd.postgre.PostgresLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSQLBankInit extends DatabaseInitializeQuery {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLBankInit.class);

    private static final String CREATE_BANK_TABLE = """
            CREATE TABLE IF NOT EXISTS island_bank (
                island_id uuid PRIMARY KEY,
                balance double precision NOT NULL DEFAULT 0
            );
            """;

    private static DatabaseLoader database;
    private static PostgreSQLBankGenerator postgresBankGenerator;

    public PostgreSQLBankInit() {
        initializeDatabase();
    }

    public static DatabaseLoader getPool() {
        return database;
    }

    public static PostgreSQLBankGenerator getPostgresBankGenerator() {
        return postgresBankGenerator;
    }

    private void initializeDatabase() {
        var pg = new Postgres(
                ConfigLoader.database.getPostgreConfig()
        );
        database = new PostgresLoader(pg);

        postgresBankGenerator = new PostgreSQLBankGenerator(database);
    }

    @Override
    public Boolean init() {
        try {
            if (!database.loadDatabase()) {
                return false;
            }
            SQLExecute.update(database, CREATE_BANK_TABLE, null);
            return true;
        } catch (DatabaseException e) {
            log.error("Database initialization error: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error creating island_bank table: {}", e.getMessage(), e);
            return false;
        }
    }
}
