package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IslandQuery {

    private InterneAPI api;
    private String databaseName;
    private final Logger logger;

    public IslandQuery(InterneAPI api, String databaseName) {
        this.logger = LogManager.getLogger("fr.euphyllia.skyfolia.database.query.exec.IslandQuery");
    }
}
