package fr.euphyllia.skyfolia.database.query;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.database.query.exec.IslandQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MariaDBTransactionQuery {

    private final InterneAPI api;
    private final String databaseName;
    private final Logger logger;
    private IslandQuery islandQuery;


    // Execution Query
    public MariaDBTransactionQuery(InterneAPI plugin) {
        this.api = plugin;
        this.logger = LogManager.getLogger("fr.euphyllia.skyfolia.database.query.MariaDBTransactionQuery");
        this.databaseName = ConfigToml.mariaDBConfig.database();
        this.islandQuery = new IslandQuery(this.api, this.databaseName);
    }

    public IslandQuery getIslandQuery() {
        return this.islandQuery;
    }
}
