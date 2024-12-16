package fr.euphyllia.skyllia.sgbd;

import com.zaxxer.hikari.HikariDataSource;
import fr.euphyllia.skyllia.sgbd.configuration.MariaDBConfig;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.model.DBConnect;
import fr.euphyllia.skyllia.sgbd.model.DBInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDB implements DBConnect, DBInterface {

    private final Logger logger = LogManager.getLogger(MariaDB.class);
    private final MariaDBConfig mariaDBConfig;
    private HikariDataSource pool;
    private boolean connected;

    public MariaDB(final MariaDBConfig configMariaDB) {
        this.mariaDBConfig = configMariaDB;
        this.connected = false;
    }


    @Override
    public boolean onLoad() throws DatabaseException {
        if (pool != null && !pool.isClosed()) {
            logger.warn("Le pool de connexions est déjà initialisé.");
            return connected;
        }

        this.pool = new HikariDataSource();
        this.pool.setDriverClassName("org.mariadb.jdbc.Driver");
        this.pool.setJdbcUrl("jdbc:mariadb://%s:%s/".formatted(mariaDBConfig.hostname(), mariaDBConfig.port()));
        this.pool.setUsername(mariaDBConfig.user());
        this.pool.setPassword(mariaDBConfig.pass());
        this.pool.setConnectionTimeout(mariaDBConfig.timeOut());

        this.pool.setMaximumPoolSize(mariaDBConfig.maxPool());
        this.pool.setMinimumIdle(mariaDBConfig.maxPool());

        try (Connection connection = pool.getConnection()) {
            if (connection.isValid(2)) {
                this.connected = true;
                this.logger.info("Pool MariaDB initialisé avec succès. Taille maximale du pool : {}", mariaDBConfig.maxPool());
                return true;
            }
        } catch (SQLException e) {
            this.logger.fatal("Échec de l'initialisation du pool MariaDB : {}", e.getMessage(), e);
            throw new DatabaseException("Échec de l'initialisation du pool MariaDB", e);
        }
        return false;
    }

    @Override
    public void onClose() {
        if (isConnected() && this.pool != null && !this.pool.isClosed()) {
            this.pool.close();
            connected = false;
            logger.info("Pool MariaDB fermé.");
        }
    }

    @Override
    public boolean isConnected() {
        return connected && this.pool != null && !this.pool.isClosed();
    }

    @Override
    public @Nullable Connection getConnection() throws DatabaseException {
        if (!isConnected()) {
            throw new DatabaseException("Non connecté à la base de données.");
        }

        try {
            return this.pool.getConnection();
        } catch (SQLException e) {
            this.logger.fatal("Erreur lors de l'obtention de la connexion : {}", e.getMessage(), e);
            throw new DatabaseException("Erreur lors de l'obtention de la connexion.", e);
        }
    }
}
