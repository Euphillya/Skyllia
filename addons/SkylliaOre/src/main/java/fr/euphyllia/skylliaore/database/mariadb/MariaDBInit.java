package fr.euphyllia.skylliaore.database.mariadb;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDB;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDBLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import fr.euphyllia.skylliaore.api.OreGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MariaDBInit extends DatabaseInitializeQuery {

    private static final Logger log = LoggerFactory.getLogger(MariaDBInit.class);
    private static final String CREATE_GENERATOR = """
            CREATE TABLE IF NOT EXISTS `generators` (
            `island_id` CHAR(36) NOT NULL,
            `generator_id` VARCHAR(255) NOT NULL,
            PRIMARY KEY (`island_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static DatabaseLoader database;
    private static OreGenerator mariaDbGenerator;

    public MariaDBInit() {
        initializeDatabase();
    }

    public static DatabaseLoader getPool() {
        return database;
    }

    public static OreGenerator getMariaDbGenerator() {
        return mariaDbGenerator;
    }

    private void initializeDatabase() {
        MariaDB mariaDB = new MariaDB(ConfigLoader.database.getMariaDBConfig());
        database = new MariaDBLoader(mariaDB);
        mariaDbGenerator = new MariaDBGenerator(database);
    }

    @Override
    public Boolean init() {
        try {
            if (!database.loadDatabase()) {
                return false;
            }
            createGeneratorTable();
            return true;
        } catch (DatabaseException e) {
            log.error("Database initialization error: {}", e.getMessage(), e);
            return false;
        }
    }

    private void createGeneratorTable() {
        try {
            SQLExecute.update(database,
                    CREATE_GENERATOR, null);
        } catch (Exception exception) {
            log.error("Error creating generator table: {}", exception.getMessage(), exception);
        }
    }
}
