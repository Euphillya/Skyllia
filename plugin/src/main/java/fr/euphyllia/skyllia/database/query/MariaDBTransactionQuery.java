package fr.euphyllia.skyllia.database.query;

import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MariaDBTransactionQuery {
    private final String databaseName;
    private final Logger logger = LogManager.getLogger(this);


    // Execution Query
    public MariaDBTransactionQuery() {
        this.databaseName = ConfigToml.mariaDBConfig.database();
    }

    public String getDatabaseName() {
        return this.databaseName;
    }
}
