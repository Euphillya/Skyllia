package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.managers.ConfigManager;
import fr.euphyllia.skyllia.sgbd.MariaDB;
import fr.euphyllia.skyllia.sgbd.configuration.MariaDBConfig;
import org.jetbrains.annotations.Nullable;

public class DatabaseConfigManager implements ConfigManager {

    private final CommentedFileConfig config;
    private MariaDBConfig mariaDBConfig;

    public DatabaseConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        // MariaDB
        boolean enabledMariaDB = config.getOrElse("mariadb.enabled", true);
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

        if (enabledMariaDB) this.mariaDBConfig = new MariaDBConfig(hostname, String.valueOf(port), username, password, useSSL, maxPool, minPool, timeOut, maxLifeTime, keepAliveTime, database);

        // Todo ? Autre database
    }

    public @Nullable MariaDBConfig getMariaDBConfig() {
        return mariaDBConfig;
    }
}