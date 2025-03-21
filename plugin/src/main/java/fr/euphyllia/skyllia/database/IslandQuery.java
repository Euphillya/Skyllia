package fr.euphyllia.skyllia.database;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.*;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.database.mariadb.MariaDBDatabaseInitialize;
import fr.euphyllia.skyllia.database.mariadb.exec.*;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IslandQuery {

    private final Logger logger = LogManager.getLogger(IslandQuery.class);
    private final InterneAPI api;
    private final String databaseName;
    private DatabaseInitializeQuery databaseInitializeQuery;
    private IslandDataQuery islandDataQuery;
    private IslandUpdateQuery islandUpdateQuery;
    private IslandWarpQuery islandWarpQuery;
    private IslandMemberQuery islandMemberQuery;
    private IslandPermissionQuery islandPermissionQuery;

    public IslandQuery(InterneAPI api, String databaseName) {
        this.api = api;
        this.databaseName = databaseName;
        try {
            this.init();
        } catch (DatabaseException exception) {
            logger.log(Level.FATAL, exception.getMessage(), exception);
        }
    }

    private void init() throws DatabaseException {
        // Todo future support database
        if (ConfigLoader.database.getMariaDBConfig() != null) {
            this.databaseInitializeQuery = new MariaDBDatabaseInitialize(this.api);
            this.islandDataQuery = new MariaDBIslandData(api, databaseName);
            this.islandUpdateQuery = new MariaDBIslandUpdate(api, databaseName);
            this.islandWarpQuery = new MariaDBIslandWarp(api, databaseName);
            this.islandMemberQuery = new MariaDBIslandMember(api, databaseName);
            this.islandPermissionQuery = new MariaDBIslandPermission(api, databaseName);
        }
        if (ConfigLoader.database.getSqLiteConfig() != null) {

        }

        throw new DatabaseException("No Database configured!");
    }

    public DatabaseInitializeQuery getDatabaseInitializeQuery() {
        return this.databaseInitializeQuery;
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

    public IslandPermissionQuery getIslandPermissionQuery() {
        return this.islandPermissionQuery;
    }
}
