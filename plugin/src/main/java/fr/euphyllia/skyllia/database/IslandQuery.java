package fr.euphyllia.skyllia.database;

import fr.euphyllia.skyllia.api.InterneAPI;
import fr.euphyllia.skyllia.api.database.*;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.database.mariadb.*;
import fr.euphyllia.skyllia.database.postgresql.*;
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
        final DatabaseLoader loader = this.api.getDatabaseLoader();
        if (loader == null) throw new DatabaseException("Database loader is not initialized.");

        // --- MariaDB ---
        if (ConfigLoader.database.getMariaDBConfig() != null) {
            final String dbName = ConfigLoader.database.getMariaDBConfig().database();

            this.databaseInitializeQuery = new MariaDBDatabaseInitialize(loader);
            this.islandDataQuery = new MariaDBIslandData(loader);
            this.islandUpdateQuery = new MariaDBIslandUpdate(loader);
            this.islandWarpQuery = new MariaDBIslandWarp(loader);
            this.islandMemberQuery = new MariaDBIslandMember(loader);
            // TODO permissions :
            // this.islandPermissionQuery = new MariaDBIslandPermission(loader);

            return;
        }

        // --- PostgreSQL ---
        if (ConfigLoader.database.getPostgreConfig() != null) {
            // selon ton config object :
            // final String dbName = ConfigLoader.database.getPostgreConfig().database();

            this.databaseInitializeQuery = new PostgreSQLDatabaseInitialize(loader);
            this.islandDataQuery = new PostgreSQLIslandData(loader);
            this.islandUpdateQuery = new PostgreSQLIslandUpdate(loader);
            this.islandWarpQuery = new PostgreSQLIslandWarp(loader);
            this.islandMemberQuery = new PostgreSQLIslandMember(loader);

            // TODO permissions :
            // this.islandPermissionQuery = new PostgreSQLIslandPermission(loader);

            return;
        }

        // --- SQLite ---
        if (ConfigLoader.database.getSqLiteConfig() != null) {
            if (!(loader instanceof SQLiteDatabaseLoader sqliteLoader)) {
                throw new DatabaseException(
                        "SQLite config is set but DatabaseLoader is not SQLiteDatabaseLoader (got: " + loader.getClass().getName() + ")"
                );
            }

            this.databaseInitializeQuery = new SQLiteDatabaseInitialize(sqliteLoader);
            this.islandDataQuery = new SQLiteIslandData(sqliteLoader);
            this.islandUpdateQuery = new SQLiteIslandUpdate(sqliteLoader);
            this.islandWarpQuery = new SQLiteIslandWarp(sqliteLoader);
            this.islandMemberQuery = new SQLiteIslandMember(sqliteLoader);

            // TODO permissions :
            // this.islandPermissionQuery = new SQLiteIslandPermission(sqliteLoader);

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
