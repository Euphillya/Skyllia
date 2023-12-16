package fr.euphyllia.skyfolia.database.query;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.configuration.section.MariaDBConfig;
import fr.euphyllia.skyfolia.database.execute.MariaDBExecute;
import fr.euphyllia.skyfolia.utils.exception.DatabaseException;

import java.sql.SQLException;

public class MariaDBCreateTable {

    private final static String CREATE_DATABASE = """
            CREATE DATABASE IF NOT EXISTS %s;
            """;

    private final String database;

    public MariaDBCreateTable(Main main) throws DatabaseException {
        MariaDBConfig dbConfig = ConfigToml.mariaDBConfig;
        if (dbConfig == null) {
            throw new DatabaseException("No database is mentioned in the configuration of the plugin.", null);
        }
        this.database = ConfigToml.mariaDBConfig.database();
        try {
            this.init();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void init() throws SQLException {
        // DATABASE
        MariaDBExecute.executeQuery(CREATE_DATABASE.formatted(this.database), null, null, null);

    }
}
