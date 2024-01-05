package fr.euphyllia.skyfolia.database.query.exec;

import fr.euphyllia.skyfolia.api.InterneAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IslandQuery {


    private final Logger logger = LogManager.getLogger(IslandQuery.class);
    private final InterneAPI api;
    private final String databaseName;
    private final IslandDataQuery islandDataQuery;
    private final IslandUpdateQuery islandUpdateQuery;
    private final IslandWarpQuery islandWarpQuery;
    private final IslandMemberQuery islandMemberQuery;

    public IslandQuery(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
        this.islandDataQuery = new IslandDataQuery(api, databaseName);
        this.islandUpdateQuery = new IslandUpdateQuery(api, databaseName);
        this.islandWarpQuery = new IslandWarpQuery(api, databaseName);
        this.islandMemberQuery = new IslandMemberQuery(api, databaseName);
    }

    public IslandDataQuery getIslandDataQuery() {
        return this.islandDataQuery;
    }

    public IslandUpdateQuery getIslandUpdateQuery() {
        return this.islandUpdateQuery;
    }

    public IslandWarpQuery getIslandWarpQuery() {
        return this.islandWarpQuery;
    }

    public IslandMemberQuery getIslandMemberQuery() {
        return this.islandMemberQuery;
    }

}
