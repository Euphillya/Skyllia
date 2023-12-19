package fr.euphyllia.skyfolia.database.sgbd;

import com.zaxxer.hikari.HikariDataSource;
import fr.euphyllia.skyfolia.configuration.section.MariaDBConfig;
import fr.euphyllia.skyfolia.database.model.DBConnect;
import fr.euphyllia.skyfolia.database.model.DBInterface;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDB implements DBConnect, DBInterface {

    private final Logger logger;
    private final MariaDBConfig mariaDBConfig;
    private HikariDataSource pool;
    private boolean connected = false;

    public MariaDB(final MariaDBConfig configMariaDB) {
        this.logger = LogManager.getLogger("fr.euphyllia.skyfolia.database.sgbd.MariaDB");
        this.mariaDBConfig = configMariaDB;
        this.connected = false;
    }


    @Override
    public boolean onLoad() {
        this.pool = new HikariDataSource();
        this.pool.setDriverClassName("org.mariadb.jdbc.Driver");
        this.pool.setJdbcUrl("jdbc:mariadb://%s:%s/".formatted(mariaDBConfig.hostname(), mariaDBConfig.port()));
        this.pool.setUsername(mariaDBConfig.user());
        this.pool.setPassword(mariaDBConfig.pass());
        this.pool.setMaximumPoolSize(mariaDBConfig.maxPool());
        this.pool.setMinimumIdle(mariaDBConfig.maxPool());
        this.pool.setConnectionTimeout(mariaDBConfig.timeOut());

        try (Connection connection = pool.getConnection()) {
            if (connection.isValid(1)) {
                this.connected = true;
                this.logger.log(Level.INFO, "MariaDB pool initialized (" + mariaDBConfig.maxPool() + ")");
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.FATAL, e.getMessage());
        }
        return false;
    }

    @Override
    public void onClose() {
        if (this.isConnected()) {
            this.pool.close();
        }
    }

    @Override
    public boolean isConnected() {
        return this.connected;
    }

    @Override
    public @Nullable Connection getConnection() {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            logger.log(Level.FATAL, e.getMessage());
        }
        return null;
    }
}
