package fr.euphyllia.skyfolia.api;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.database.DatabaseLoader;
import fr.euphyllia.skyfolia.database.query.MariaDBCreateTable;
import fr.euphyllia.skyfolia.database.query.MariaDBTransactionQuery;
import fr.euphyllia.skyfolia.database.sgbd.MariaDB;
import fr.euphyllia.skyfolia.utils.exception.DatabaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class InterneAPI {

    private @Nullable DatabaseLoader database;
    private Main instancePlugin;
    private final Logger logger;
    private MariaDBTransactionQuery transaction;
    private DatabaseLoader databaseLoader;

    public InterneAPI(Main plugin) {
        this.instancePlugin = plugin;
        this.logger = LogManager.getLogger("fr.euphyllia.skyfolia.api.InterneAPI");
    }

    public Main getPluginInstance() {
        return this.instancePlugin;
    }

    public @Nullable DatabaseLoader getDatabaseLoader() {
        return this.database;
    }

    public boolean setupConfigs(String fileName) {
        if (!this.instancePlugin.getDataFolder().exists() && (!this.instancePlugin.getDataFolder().mkdir())) {
            logger.log(Level.FATAL, "Unable to create the configuration folder.");
            return false;

        }
        FileSystem fs = FileSystems.getDefault();
        Path configFolder = fs.getPath(this.instancePlugin.getDataFolder().getAbsolutePath());

        File configFile = new File(configFolder + File.separator + fileName);
        if (!configFile.exists()) {
            this.instancePlugin.saveResource(fileName, false);
        }

        try {
            ConfigToml.init(configFile);
        } catch (Exception databaseException) {
            logger.log(Level.FATAL, databaseException.getMessage());
            return false;
        }
        return true;
    }

    public boolean setupSGBD() {
        MariaDB mariaDB;
        if (ConfigToml.mariaDBConfig != null) {
            mariaDB = new MariaDB(this.instancePlugin, ConfigToml.mariaDBConfig);
            this.database = new DatabaseLoader(this.instancePlugin, mariaDB);
            if (!this.database.loadDatabase()) {
                return false;
            }
            try {
                new MariaDBCreateTable(this.instancePlugin);
            } catch (DatabaseException exception) {
                exception.printStackTrace();
                return false;
            }
            try {
                this.transaction = new MariaDBTransactionQuery(this.instancePlugin);
            } catch (DatabaseException exception) {
                exception.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
