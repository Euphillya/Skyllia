package fr.euphyllia.skyllia.configuration.model;

import fr.euphyllia.sgbd.configuration.MariaDBConfig;
import fr.euphyllia.skyllia.api.configuration.DatabaseType;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import org.apache.logging.log4j.Level;

public class MariaDB extends ConfigToml {

    private final String path = "sgbd.mariadb.%s";
    private final int dbVersion = 3;

    private DatabaseType databaseType() {
        String value = getString(path.formatted("type"), DatabaseType.MARIADB.name());
        try {
            return DatabaseType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            log(Level.ERROR, "%s is not supported ! MariaDB selected.".formatted(value));
            return DatabaseType.MARIADB;
        }
    }

    private String hostname() {
        return getString(path.formatted("hostname"), "127.0.0.1");
    }

    private String port() {
        String port = "3306";
        if (dbVersion < 3) {
            port = getString(path.formatted("host"), port);
            remove(path.formatted("host"));
        }
        return getString(path.formatted("port"), port);
    }

    private String username() {
        return getString(path.formatted("username"), "admin");
    }

    private String password() {
        return getString(path.formatted("password"), "azerty123@");
    }

    private boolean useSSL() {
        return getBoolean(path.formatted("useSSL"), false);
    }

    private int maxPool() {
        return getInt(path.formatted("maxPool"), 5);
    }

    private int timeOut() {
        return getInt(path.formatted("timeOut"), 500);
    }

    private String database() {
        return getString(path.formatted("database"), "skyllia");
    }

    private int version() {
        remove(path.formatted("version"));
        return getInt(path.formatted("version"), dbVersion);
    }

    public MariaDBConfig getConstructor() {
        return new MariaDBConfig(hostname(), port(), username(), password(), useSSL(), maxPool(), timeOut(), database(), version());
    }
}
