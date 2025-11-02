package fr.euphyllia.skyllia.sgbd.postgre;

import com.zaxxer.hikari.HikariDataSource;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.model.DBConnect;
import fr.euphyllia.skyllia.sgbd.model.DBInterface;
import fr.euphyllia.skyllia.sgbd.postgre.configuration.PostgresConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public class Postgres implements DBConnect, DBInterface {

    private final Logger logger = LogManager.getLogger(Postgres.class);
    private final PostgresConfig cfg;
    private HikariDataSource pool;
    private boolean connected;

    public Postgres(final PostgresConfig cfg) {
        this.cfg = cfg;
        this.connected = false;
    }


    /**
     * Initializes and loads the database connection.
     *
     * @return {@code true} if the connection is successfully established,
     * {@code false} otherwise
     * @throws DatabaseException if any error occurs during initialization
     */
    @Override
    public boolean onLoad() throws DatabaseException {
        if (pool != null && !pool.isClosed()) return connected;

        this.pool = new HikariDataSource();
        this.pool.setPoolName("skyllia-pg-hikari");
        this.pool.setDriverClassName("org.postgresql.Driver");

        this.pool.setJdbcUrl("jdbc:postgresql://%s:%s/%s".formatted(
                cfg.hostname(), cfg.port(), cfg.database()));
        this.pool.setUsername(cfg.user());
        this.pool.setPassword(cfg.pass());

        this.pool.addDataSourceProperty("reWriteBatchedInserts", "true");

        this.pool.addDataSourceProperty("stringtype", "unspecified");

        this.pool.setMaximumPoolSize(cfg.maxPool().intValue());
        this.pool.setMinimumIdle(cfg.minPool().intValue());
        this.pool.setMaxLifetime(cfg.maxLifeTime().intValue());
        this.pool.setKeepaliveTime(cfg.keepAliveTime().intValue());
        this.pool.setConnectionTimeout(cfg.timeOut().intValue());

        try (Connection c = pool.getConnection()) {
            if (c.isValid(2)) {
                this.connected = true;
                this.logger.info(
                        "PostgreSQL pool initialized successfully. Minimum pool size: {}, Maximum pool size: {}",
                        cfg.minPool(),
                        cfg.maxPool()
                );
                return true;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize PostgreSQL pool", e);
        }
        return false;

    }

    /**
     * Closes the database connection if it is currently open.
     */
    @Override
    public void onClose() {
        if (isConnected() && pool != null && !pool.isClosed()) {
            pool.close();
            connected = false;
            logger.info("PostgreSQL pool closed.");
        }
    }

    /**
     * Checks whether the database connection is active and valid.
     *
     * @return {@code true} if the database is connected, {@code false} otherwise
     */
    @Override
    public boolean isConnected() {
        return connected && pool != null && !pool.isClosed();
    }

    /**
     * Retrieves a valid {@link Connection} to the database.
     *
     * @return a {@link Connection}, or {@code null} if no connection could be established
     * @throws DatabaseException if an error occurs while obtaining the connection
     */
    @Override
    public @Nullable Connection getConnection() throws DatabaseException {
        if (pool == null) throw new DatabaseException("Pool is null");
        if (!isConnected()) throw new DatabaseException("Not connected");
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            throw new DatabaseException("getConnection failed", e);
        }
    }
}
