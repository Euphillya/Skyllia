package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.managers.ConfigManager;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.configuration.MariaDBConfig;
import fr.euphyllia.skyllia.sgbd.sqlite.configuration.SQLiteConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DatabaseConfigManager implements ConfigManager {

    private static final Logger log = LogManager.getLogger(DatabaseConfigManager.class);
    private final CommentedFileConfig config;
    private int configVersion;
    private MariaDBConfig mariaDBConfig;
    private SQLiteConfig sqLiteConfig;

    public DatabaseConfigManager(CommentedFileConfig config) {
        this.config = config;
        try {
            loadConfig();
        } catch (DatabaseException exception) {
            log.error(exception);
        }
    }

    @Override
    public void loadConfig() throws DatabaseException {
        configVersion = config.getOrElse("config-version", 3);
        // MariaDB
        boolean enabledMariaDB = config.getOrElse("mariadb.enabled", true);
        if (enabledMariaDB) {
            String hostname = config.getOrElse("mariadb.hostname", "127.0.0.1");
            int port = config.getOrElse("mariadb.port", 3306);
            String database = config.getOrElse("mariadb.database", "skyblock");
            String username = config.getOrElse("mariadb.username", "user");
            String password = config.getOrElse("mariadb.password", "password");
            boolean useSSL = config.getOrElse("mariadb.useSSL", false);
            int minPool = config.getOrElse("mariadb.minPool", 1);
            int maxPool = config.getOrElse("mariadb.maxPool", 10);
            long keepAliveTime = config.getOrElse("mariadb.keepAliveTime", 0);
            long maxLifeTime = config.getOrElse("mariadb.maxLifeTime", 1800000);
            int timeOut = config.getOrElse("mariadb.timeOut", 5000);
            this.mariaDBConfig = new MariaDBConfig(hostname, String.valueOf(port), username, password, useSSL, minPool, maxPool, maxLifeTime, keepAliveTime, timeOut, database);
            return;
        }

        // SQlite
        boolean enabledSQlite = config.getOrElse("sqlite.enabled", false);
        if (enabledSQlite) {
            if (true) throw new DatabaseException("SQLite is still not supported.");
            String file = config.getOrElse("sqlite.file", "skyllia.db");
            int minPool = config.getOrElse("sqlite.minPool", 1);
            int maxPool = config.getOrElse("sqlite.maxPool", 10);
            long keepAliveTime = config.getOrElse("sqlite.keepAliveTime", 0);
            long maxLifeTime = config.getOrElse("sqlite.maxLifeTime", 1800000);
            int timeOut = config.getOrElse("sqlite.timeOut", 30000);
            this.sqLiteConfig = new SQLiteConfig(file, minPool, maxPool, keepAliveTime, maxLifeTime, timeOut);
        }

        throw new DatabaseException("No Database configured!");
        // Todo ? Autre database
    }

    public @Nullable MariaDBConfig getMariaDBConfig() {
        return mariaDBConfig;
    }

    public @Nullable SQLiteConfig getSqLiteConfig() {
        return sqLiteConfig;
    }

    public int getConfigVersion() {
        return configVersion;
    }
}