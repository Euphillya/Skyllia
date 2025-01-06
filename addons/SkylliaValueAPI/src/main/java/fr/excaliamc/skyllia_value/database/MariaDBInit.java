package fr.excaliamc.skyllia_value.database;

import fr.euphyllia.skyllia.api.database.DatabaseInitializeQuery;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.sgbd.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.MariaDB;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.execute.MariaDBExecute;
import fr.excaliamc.skyllia_value.Main;

public class MariaDBInit extends DatabaseInitializeQuery {

    public MariaDBInit() {
        this.initializeDatabase();
    }

    public static DatabaseLoader getDatabaseLoader() {
        throw new RuntimeException();
    }

    private void initializeDatabase() {
        throw new RuntimeException();
    }

    public boolean init() throws DatabaseException {
        throw new RuntimeException();
    }

    public void createTable() throws DatabaseException {
        throw new RuntimeException();
    }
}
