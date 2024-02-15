package fr.euphyllia.skyllia.configuration.model;

import fr.euphyllia.skyllia.api.configuration.MariaDBConfig;
import fr.euphyllia.skyllia.configuration.ConfigToml;

public class MariaDB extends ConfigToml {

    private String path = "sgbd.mariadb.%s";
    private int dbVersion = 2;

    private String hostname() {
        return getString(path.formatted("hostname"), "127.0.0.1");
    }

    private String port() {
        return getString(path.formatted("host"), "3306");
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
        return getInt(path.formatted("version"), dbVersion);
    }

    public MariaDBConfig getConstructor() {
        return new MariaDBConfig(hostname(), port(), username(), password(), useSSL(), maxPool(), timeOut(), database(), version());
    }
}
