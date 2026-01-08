package fr.euphyllia.skyllia.sgbd.mariadb;

import com.zaxxer.hikari.HikariDataSource;
import fr.euphyllia.skyllia.sgbd.DatabaseConfig;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.utils.model.DBConnect;
import fr.euphyllia.skyllia.sgbd.utils.model.DBInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The {@code MariaDB} class is responsible for managing a MariaDB connection using
 * a HikariCP connection pool. It implements both {@link DBConnect} and {@link DBInterface},
 * providing methods to load and close the pool, check the connection status, and retrieve
 * a valid SQL connection.
 */
public class MariaDB implements DBConnect, DBInterface {

    private final Logger logger = LogManager.getLogger(MariaDB.class);
    private final DatabaseConfig mariaDBConfig;
    private HikariDataSource pool;
    private boolean connected;

    /**
     * Constructs a new {@code MariaDB} instance with the specified configuration.
     *
     * @param configMariaDB the configuration object for connecting to the MariaDB database.
     */
    public MariaDB(final DatabaseConfig configMariaDB) {
        this.mariaDBConfig = configMariaDB;
        this.connected = false;
    }

    /**
     * Initializes the HikariCP connection pool for MariaDB using the provided configuration.
     *
     * @return {@code true} if the connection pool was successfully initialized, {@code false} otherwise.
     * @throws DatabaseException if any error occurs during the pool initialization.
     */
    @Override
    public boolean onLoad() throws DatabaseException {
        if (pool != null && !pool.isClosed()) {
            logger.warn("The connection pool is already initialized.");
            return connected;
        }

        ensureDatabaseExists();

        this.pool = new HikariDataSource();
        this.pool.setPoolName("skyllia-hikari");
        this.pool.setDriverClassName("org.mariadb.jdbc.Driver");
        this.pool.setJdbcUrl("jdbc:mariadb://%s:%s/%s"
                .formatted(mariaDBConfig.hostname(), mariaDBConfig.port(), mariaDBConfig.database()));

        this.pool.setUsername(mariaDBConfig.user());
        this.pool.setPassword(mariaDBConfig.pass());

        // Configure the connection pool
        this.pool.setMaximumPoolSize(mariaDBConfig.maxPool());
        this.pool.setMinimumIdle(mariaDBConfig.minPool());
        this.pool.setMaxLifetime(mariaDBConfig.maxLifeTime());
        this.pool.setKeepaliveTime(mariaDBConfig.keepAliveTime());
        this.pool.setConnectionTimeout(mariaDBConfig.timeOut());

        try (Connection connection = pool.getConnection()) {
            if (connection.isValid(2)) {
                this.connected = true;
                this.logger.info(
                        "MariaDB pool initialized successfully. Minimum pool size: {}, Maximum pool size: {}",
                        mariaDBConfig.minPool(),
                        mariaDBConfig.maxPool()
                );
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize the MariaDB pool", e);
        }
        return false;
    }

    /**
     * Closes the HikariCP connection pool if it is currently open and connected.
     */
    @Override
    public void onClose() {
        if (isConnected() && this.pool != null && !this.pool.isClosed()) {
            this.pool.close();
            connected = false;
            logger.info("MariaDB pool has been closed.");
        }
    }

    /**
     * Indicates whether a valid connection to the database is currently active.
     *
     * @return {@code true} if the connection is active, otherwise {@code false}.
     */
    @Override
    public boolean isConnected() {
        return connected && this.pool != null && !this.pool.isClosed();
    }

    /**
     * Retrieves a {@link Connection} from the HikariCP connection pool.
     *
     * @return a valid {@link Connection} to the MariaDB database.
     * @throws DatabaseException if the pool is not initialized or if retrieving the connection fails.
     */
    @Override
    public @Nullable Connection getConnection() throws DatabaseException {
        if (this.pool == null) {
            throw new DatabaseException("Unable to get a connection from the pool (pool is null).");
        }

        if (!isConnected()) {
            throw new DatabaseException("Not connected to the database.");
        }

        try {
            return this.pool.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("Unable to get a connection from the pool (getConnection returned null).", e);
        }
    }

    private void ensureDatabaseExists() throws DatabaseException {
        String bootstrapUrl = "jdbc:mariadb://%s:%s/"
                .formatted(mariaDBConfig.hostname(), mariaDBConfig.port());

        try (Connection connection = java.sql.DriverManager.getConnection(
                bootstrapUrl,
                mariaDBConfig.user(),
                mariaDBConfig.pass()
        );
             var statement = connection.createStatement()) {

            statement.execute(
                    "CREATE DATABASE IF NOT EXISTS `%s`"
                            .formatted(mariaDBConfig.database())
            );

            logger.info("Database '{}' ensured.", mariaDBConfig.database());

        } catch (SQLException e) {
            throw new DatabaseException("Failed to create database if not exists", e);
        }
    }

}
