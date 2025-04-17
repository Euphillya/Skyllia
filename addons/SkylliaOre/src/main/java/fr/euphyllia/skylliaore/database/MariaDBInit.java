package fr.euphyllia.skylliaore.database;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDB;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDBLoader;
import fr.euphyllia.skyllia.sgbd.mariadb.execute.MariaDBExecute;
import fr.euphyllia.skyllia.sgbd.model.DatabaseLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MariaDBInit extends DatabaseInitializeQuery {

    private static final Logger log = LoggerFactory.getLogger(MariaDBInit.class);
    private static final String CREATE_GENERATOR = """
            CREATE TABLE IF NOT EXISTS `%s`.`generators` (
            `island_id` CHAR(36) NOT NULL,
            `generator_id` VARCHAR(255) NOT NULL,
            PRIMARY KEY (`island_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
            """;
    private static DatabaseLoader database;
    private static MariaDBGenerator mariaDbGenerator;

    public MariaDBInit() {
        initializeDatabase();
        initializeGenerator();
    }

    public static DatabaseLoader getPool() {
        return database;
    }

    public static MariaDBGenerator getMariaDbGenerator() {
        return mariaDbGenerator;
    }

    private void initializeDatabase() {
        MariaDB mariaDB = new MariaDB(ConfigLoader.database.getMariaDBConfig());
        database = new MariaDBLoader(mariaDB);
    }

    private void initializeGenerator() {
        mariaDbGenerator = new MariaDBGenerator();
    }

    @Override
    public boolean init() throws DatabaseException {
        if (!database.loadDatabase()) {
            return false;
        }
        createGeneratorTable();
        return true;
    }

    private void createGeneratorTable() {
        try {
            MariaDBExecute.executeQuery(database,
                    CREATE_GENERATOR.formatted(ConfigLoader.database.getMariaDBConfig().database()));
        } catch (Exception exception) {
            log.error("Error creating generator table: {}", exception.getMessage(), exception);
        }
    }
}
