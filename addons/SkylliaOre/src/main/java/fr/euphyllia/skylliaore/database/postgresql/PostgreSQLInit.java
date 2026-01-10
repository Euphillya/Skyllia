package fr.euphyllia.skylliaore.database.postgresql;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.postgre.Postgres;
import fr.euphyllia.skyllia.sgbd.postgre.PostgresLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.sql.SQLExecute;
import fr.euphyllia.skylliaore.api.OreGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSQLInit extends DatabaseInitializeQuery {

    private static final Logger log = LoggerFactory.getLogger(PostgreSQLInit.class);

    private static final String CREATE_GENERATOR = """
            CREATE TABLE IF NOT EXISTS generators (
                island_id uuid PRIMARY KEY,
                generator_id varchar(255) NOT NULL
            );
            """;

    private static DatabaseLoader database;
    private static OreGenerator postgresGenerator;

    public PostgreSQLInit() {
        initializeDatabase();
    }

    public static DatabaseLoader getPool() {
        return database;
    }

    public static OreGenerator getPostgresGenerator() {
        return postgresGenerator;
    }

    private void initializeDatabase() {
        Postgres postgres = new Postgres(ConfigLoader.database.getPostgreConfig());
        database = new PostgresLoader(postgres);

        postgresGenerator = new PostgreSQLGenerator(database);
    }

    @Override
    public Boolean init() {
        try {
            if (!database.loadDatabase()) {
                return false;
            }
            SQLExecute.update(database, CREATE_GENERATOR, null);
            return true;
        } catch (DatabaseException e) {
            log.error("Database initialization error: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Error creating generators table: {}", e.getMessage(), e);
            return false;
        }
    }
}
