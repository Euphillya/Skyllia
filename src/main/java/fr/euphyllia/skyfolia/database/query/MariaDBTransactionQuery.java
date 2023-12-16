package fr.euphyllia.skyfolia.database.query;

import fr.euphyllia.skyfolia.api.InterneAPI;
import fr.euphyllia.skyfolia.utils.exception.DatabaseException;

public class MariaDBTransactionQuery {

    private final InterneAPI api;

    // Execution Query
    public MariaDBTransactionQuery(InterneAPI plugin) throws DatabaseException {
        this.api = plugin;
    }
}
