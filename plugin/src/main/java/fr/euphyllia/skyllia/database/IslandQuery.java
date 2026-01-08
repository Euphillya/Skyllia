package fr.euphyllia.skyllia.database;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.*;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.database.mariadb.*;
import fr.euphyllia.skyllia.database.sqlite.*;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IslandQuery {

    private final Logger logger = LogManager.getLogger(IslandQuery.class);
    private final InterneAPI api;
    private DatabaseInitializeQuery databaseInitializeQuery;
    private IslandDataQuery islandDataQuery;
    private IslandUpdateQuery islandUpdateQuery;
    private IslandWarpQuery islandWarpQuery;
    private IslandMemberQuery islandMemberQuery;
    private IslandPermissionQuery islandPermissionQuery;

    public IslandQuery(InterneAPI api) {
        this.api = api;
        try {
            this.init();
        } catch (DatabaseException exception) {
            logger.log(Level.FATAL, exception.getMessage(), exception);
        }
    }

    private void init() throws DatabaseException {
        // Todo future support database
        if (ConfigLoader.database.getMariaDBConfig() != null) {
            DatabaseLoader loader = this.api.getDatabaseLoader();
            if (loader == null) {
                throw new DatabaseException("Database loader is not initialized.");
            }
            this.databaseInitializeQuery = new MariaDBDatabaseInitialize(loader);
            this.islandDataQuery = new MariaDBIslandData(loader);
            this.islandUpdateQuery = new MariaDBIslandUpdate(loader);
            this.islandWarpQuery = new MariaDBIslandWarp(loader);
            this.islandMemberQuery = new MariaDBIslandMember(loader);
            return;
        }
        if (ConfigLoader.database.getSqLiteConfig() != null) {
            SQLiteDatabaseLoader loader = (SQLiteDatabaseLoader) this.api.getDatabaseLoader();

            this.databaseInitializeQuery = new SQLiteDatabaseInitialize(loader);
            this.islandDataQuery = new SQLiteIslandData(loader);
            this.islandUpdateQuery = new SQLiteIslandUpdate(loader);
            this.islandWarpQuery = new SQLiteIslandWarp(loader);
            this.islandMemberQuery = new SQLiteIslandMember(loader);
            return;
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
