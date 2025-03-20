package fr.euphyllia.skyllia.configuration.old_config.model;

import fr.euphyllia.skyllia.api.configuration.DatabaseType;
import fr.euphyllia.skyllia.configuration.OldConfigToml;
import fr.euphyllia.skyllia.sgbd.configuration.MariaDBConfig;
import org.apache.logging.log4j.Level;

public class MariaDB extends OldConfigToml {

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

    private int minPool() {
        return getInt(path.formatted("minPool"), 3);
    }

    private Long maxLifeTime() {
        return getLong(path.formatted("maxLifeTime"), 1800000L);
    }

    private Long keepAliveTime() {
        return getLong(path.formatted("keepAliveTime"), 0L);
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
        return new MariaDBConfig(hostname(), port(), username(), password(), useSSL(), minPool(), maxPool(), timeOut(), maxLifeTime(), keepAliveTime(), database(), version());
    }
}
