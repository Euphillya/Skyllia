package fr.euphyllia.skyfolia.api;

import fr.euphyllia.skyfolia.Main;
import fr.euphyllia.skyfolia.configuration.ConfigToml;
import fr.euphyllia.skyfolia.database.DatabaseLoader;
import fr.euphyllia.skyfolia.database.query.MariaDBCreateTable;
import fr.euphyllia.skyfolia.database.query.MariaDBTransactionQuery;
import fr.euphyllia.skyfolia.database.query.exec.IslandQuery;
import fr.euphyllia.skyfolia.database.sgbd.MariaDB;
import fr.euphyllia.skyfolia.managers.Managers;
import fr.euphyllia.skyfolia.utils.exception.DatabaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class InterneAPI {

    private @Nullable DatabaseLoader database;
    private final Logger logger;
    private MariaDBTransactionQuery transaction;
    private DatabaseLoader databaseLoader;
    private Managers managers;
    private final Main plugin;

    public InterneAPI(Main plugin) {
        this.plugin = plugin;
        this.logger = LogManager.getLogger("fr.euphyllia.skyfolia.api.InterneAPI");
    }


    public @Nullable DatabaseLoader getDatabaseLoader() {
        return this.database;
    }

    public Managers getManagers() {
        return managers;
    }

    public void setManagers(Managers managers) {
        this.managers = managers;
    }

    public boolean setupConfigs(File dataFolder, String fileName) throws IOException {
        if (!dataFolder.exists() && (!dataFolder.mkdir())) {
            logger.log(Level.FATAL, "Unable to create the configuration folder.");
            return false;

        }
        FileSystem fs = FileSystems.getDefault();
        Path configFolder = fs.getPath(dataFolder.getAbsolutePath());

        File configFile = new File(configFolder + File.separator + fileName);
        if (!configFile.exists()) {
            configFile.createNewFile();
        }

        try {
            ConfigToml.init(configFile);
        } catch (Exception databaseException) {
            logger.log(Level.FATAL, databaseException.getMessage());
            return false;
        }
        return true;
    }

    public boolean setupSGBD() throws DatabaseException {
        MariaDB mariaDB;
        if (ConfigToml.mariaDBConfig != null) {
            mariaDB = new MariaDB(ConfigToml.mariaDBConfig);
            this.database = new DatabaseLoader(this.plugin, mariaDB);
            if (!this.database.loadDatabase()) {
                return false;
            }
            new MariaDBCreateTable(this);
            this.transaction = new MariaDBTransactionQuery();
        } else {
            return false;
        }
        return true;
    }

    public IslandQuery getIslandQuery() {
        return new IslandQuery(this, this.transaction.getDatabaseName());
    }
}
