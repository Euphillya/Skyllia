package fr.euphyllia.skyfolia.database.sgbd;

import com.zaxxer.hikari.HikariDataSource;
import fr.euphyllia.skyfolia.configuration.section.MariaDBConfig;
import fr.euphyllia.skyfolia.database.model.DBConnect;
import fr.euphyllia.skyfolia.database.model.DBInterface;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

public class MariaDB implements DBConnect, DBInterface {

    private final MariaDBConfig mariaDBConfig;
    private final JavaPlugin plugin;
    private HikariDataSource pool;
    private boolean connected = false;

    public MariaDB(JavaPlugin javaPlugin, final MariaDBConfig configMariaDB) {
        this.plugin = javaPlugin;
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
                this.plugin.getLogger().log(Level.INFO, "MariaDB pool initialized (" + mariaDBConfig.maxPool() + ")");
                return true;
            }
        } catch (SQLException e) {
            this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
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
            this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
        }
        return null;
    }
}
