package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.IndentStyle;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
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
    private boolean changed = false;

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
        changed = false;
        configVersion = getOrSetDefault("config-version", 3, Integer.class);
        // MariaDB
        boolean enabledMariaDB = getOrSetDefault("mariadb.enabled", true, Boolean.class);
        if (enabledMariaDB) {
            String hostname = getOrSetDefault("mariadb.hostname", "127.0.0.1", String.class);
            int port = getOrSetDefault("mariadb.port", 3306, Integer.class);
            String database = getOrSetDefault("mariadb.database", "skyblock", String.class);
            String username = getOrSetDefault("mariadb.username", "user", String.class);
            String password = getOrSetDefault("mariadb.password", "password", String.class);
            boolean useSSL = getOrSetDefault("mariadb.useSSL", false, Boolean.class);
            int minPool = getOrSetDefault("mariadb.minPool", 1, Integer.class);
            int maxPool = getOrSetDefault("mariadb.maxPool", 10, Integer.class);
            long keepAliveTime = getOrSetDefault("mariadb.keepAliveTime", 0L, Long.class);
            long maxLifeTime = getOrSetDefault("mariadb.maxLifeTime", 1800000L, Long.class);
            int timeOut = getOrSetDefault("mariadb.timeOut", 5000, Integer.class);
            this.mariaDBConfig = new MariaDBConfig(hostname, String.valueOf(port), username, password, useSSL, minPool, maxPool, maxLifeTime, keepAliveTime, timeOut, database);

            if (changed) config.save();
            return;
        }

        // SQlite
        boolean enabledSQlite = getOrSetDefault("sqlite.enabled", false, Boolean.class);
        if (enabledSQlite) {
            String file = getOrSetDefault("sqlite.file", "plugins/Skyllia/skyllia.db", String.class);
            int minPool = getOrSetDefault("sqlite.minPool", 1, Integer.class);
            int maxPool = getOrSetDefault("sqlite.maxPool", 10, Integer.class);
            long keepAliveTime = getOrSetDefault("sqlite.keepAliveTime", 0L, Long.class);
            long maxLifeTime = getOrSetDefault("sqlite.maxLifeTime", 1800000L, Long.class);
            int timeOut = getOrSetDefault("sqlite.timeOut", 30000, Integer.class);
            this.sqLiteConfig = new SQLiteConfig(file, minPool, maxPool, keepAliveTime, maxLifeTime, timeOut);

            if (changed) {
                TomlWriter tomlWriter = new TomlWriter();
                tomlWriter.setIndent(IndentStyle.NONE);
                tomlWriter.write(config, config.getFile(), WritingMode.REPLACE);
            }
            return;
        }

        throw new DatabaseException("No Database configured!");
        // Todo ? Autre database
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrSetDefault(String path, T defaultValue, Class<T> expectedClass) {
        Object value = config.get(path);
        if (value == null) {
            config.set(path, defaultValue);
            changed = true;
            return defaultValue;
        }

        if (expectedClass.isInstance(value)) {
            return (T) value; // Bonne instance directement
        }

        // Cas spécial : Integer → Long
        if (expectedClass == Long.class && value instanceof Integer) {
            return (T) Long.valueOf((Integer) value);
        }

        // Cas spécial : Double → Float
        if (expectedClass == Float.class && value instanceof Double) {
            return (T) Float.valueOf(((Double) value).floatValue());
        }

        throw new IllegalStateException("Cannot convert value at path '" + path + "' from " + value.getClass().getSimpleName() + " to " + expectedClass.getSimpleName());
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