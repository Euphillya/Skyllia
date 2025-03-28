package fr.euphyllia.skyllia.sgbd.sqlite;

import com.zaxxer.hikari.HikariDataSource;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.model.DBConnect;
import fr.euphyllia.skyllia.sgbd.model.DBInterface;
import fr.euphyllia.skyllia.sgbd.sqlite.configuration.SQLiteConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLite implements DBConnect, DBInterface {

    private final Logger logger = LogManager.getLogger(SQLite.class);
    private final SQLiteConfig sqliteConfig;
    private HikariDataSource pool;
    private boolean connected;

    public SQLite(final SQLiteConfig sqliteConfig) {
        this.sqliteConfig = sqliteConfig;
        this.connected = false;
    }

    @Override
    public boolean onLoad() throws DatabaseException {
        if (pool != null && !pool.isClosed()) {
            logger.warn("SQLite connection pool is already initialized.");
            return connected;
        }

        pool = new HikariDataSource();
        pool.setPoolName("skyllia-sqlite");
        pool.setDriverClassName("org.sqlite.JDBC");
        pool.setJdbcUrl("jdbc:sqlite:" + sqliteConfig.filePath());

        pool.setMaximumPoolSize(sqliteConfig.maxPool());
        pool.setMinimumIdle(sqliteConfig.minPool());
        pool.setConnectionTimeout(sqliteConfig.timeout());
        pool.setMaxLifetime(sqliteConfig.maxLifetime());
        pool.setKeepaliveTime(sqliteConfig.keepAliveTime());

        try (Connection connection = pool.getConnection(); Statement st = connection.createStatement()) {
            // Exécuter un petit SELECT 1 qui devrait réussir si tout va bien
            st.execute("SELECT 1;");
            // Si on arrive là sans exception, c’est que la connexion marche
            connected = true;
            logger.info("SQLite connection pool initialized successfully.");
            return true;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize SQLite pool", e);
        }
    }

    @Override
    public void onClose() {
        if (isConnected() && pool != null && !pool.isClosed()) {
            pool.close();
            connected = false;
            logger.info("SQLite pool has been closed.");
        }
    }

    @Override
    public boolean isConnected() {
        return connected && pool != null && !pool.isClosed();
    }

    @Override
    public Connection getConnection() throws DatabaseException {
        if (pool == null) {
            throw new DatabaseException("SQLite pool is not initialized.");
        }
        if (!isConnected()) {
            throw new DatabaseException("Not connected to SQLite database.");
        }
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to obtain connection from SQLite pool", e);
        }
    }
}