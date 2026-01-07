package fr.euphyllia.skyllia.database;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.*;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.database.mariadb.MariaDBDatabaseInitialize;
import fr.euphyllia.skyllia.database.sqlite.SQLiteDatabaseInitialize;
import fr.euphyllia.skyllia.database.sqlite.exec.*;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.database.mariadb.MariaDBIslandData;
import fr.euphyllia.skyllia.database.mariadb.MariaDBIslandMember;
import fr.euphyllia.skyllia.database.mariadb.MariaDBIslandUpdate;
import fr.euphyllia.skyllia.database.mariadb.MariaDBIslandWarp;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
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
            DatabaseLoader loader = this.api.getDatabaseLoader();
            if (loader == null) {
                throw new DatabaseException("Database loader is not initialized.");
            }
            this.databaseInitializeQuery = new MariaDBDatabaseInitialize(loader, ConfigLoader.database.getMariaDBConfig(), ConfigLoader.database.getConfigVersion(), ConfigLoader.general.getRegionDistance(), ConfigLoader.general.getMaxIslands());
            this.islandDataQuery = new MariaDBIslandData(loader, databaseName);
            this.islandUpdateQuery = new MariaDBIslandUpdate(loader, databaseName);
            this.islandWarpQuery = new MariaDBIslandWarp(loader, databaseName);
            this.islandMemberQuery = new MariaDBIslandMember(loader, databaseName);
//            this.islandPermissionQuery = new MariaDBIslandPermission(api, databaseName);
            return;
        }
        if (ConfigLoader.database.getSqLiteConfig() != null) {
            SQLiteDatabaseLoader loader = (SQLiteDatabaseLoader) this.api.getDatabaseLoader();

            this.databaseInitializeQuery = new SQLiteDatabaseInitialize(this.api, loader);
            this.islandDataQuery = new SQLiteIslandData(this.api, loader);
            this.islandUpdateQuery = new SQLiteIslandUpdate(this.api, loader);
            this.islandWarpQuery = new SQLiteIslandWarp(this.api, loader);
            this.islandMemberQuery = new SQLiteIslandMember(this.api, loader);
            this.islandPermissionQuery = new SQLiteIslandPermission(this.api, loader);
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
