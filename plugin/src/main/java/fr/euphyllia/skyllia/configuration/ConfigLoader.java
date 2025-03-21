package fr.euphyllia.skyllia.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.configuration.manager.*;
import fr.euphyllia.skyllia.managers.ConfigManager;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {

    private static final Logger logger = LogManager.getLogger(ConfigLoader.class);
    private static final List<ConfigManager> configManagers = new ArrayList<>();

    public static GeneralConfigManager general;
    public static DatabaseConfigManager database;
    public static WorldConfigManager worldManager;
    public static IslandConfigManager islandManager;
    public static PlayerConfigManager playerManager;
    public static SchematicConfigManager schematicManager;
    public static LanguageConfigManager language;

    private static CommentedFileConfig generalConfig;
    private static CommentedFileConfig databaseConfig;
    private static CommentedFileConfig worldConfig;
    private static CommentedFileConfig islandConfig;
    private static CommentedFileConfig playerConfig;
    private static CommentedFileConfig schematicConfig;

    public static void init(File allConfig) {

        File configDir = new File(allConfig, "config");

        generalConfig = loadFile(new File(configDir, "config.toml"));
        databaseConfig = loadFile(new File(configDir, "database.toml"));
        worldConfig = loadFile(new File(configDir, "worlds.toml"));
        islandConfig = loadFile(new File(configDir, "islands.toml"));
        playerConfig = loadFile(new File(configDir, "players.toml"));
        schematicConfig = loadFile(new File(configDir, "schematics.toml"));

        general = new GeneralConfigManager(generalConfig);
        database = new DatabaseConfigManager(databaseConfig);
        worldManager = new WorldConfigManager(worldConfig);
        islandManager = new IslandConfigManager(islandConfig);
        playerManager = new PlayerConfigManager(playerConfig);
        schematicManager = new SchematicConfigManager(schematicConfig);
        language = new LanguageConfigManager();

        configManagers.add(general);
        configManagers.add(database);
        configManagers.add(worldManager);
        configManagers.add(islandManager);
        configManagers.add(playerManager);
        configManagers.add(schematicManager);
        configManagers.add(language);

        reloadConfigs();

        logger.log(Level.INFO, "[Config] Configurations loaded successfully.");
    }

    private static CommentedFileConfig loadFile(File file) {
        CommentedFileConfig configFile = CommentedFileConfig.builder(file).sync().autosave().build();
        configFile.load();
        return configFile;
    }

    public static void reloadConfigs() {
        logger.log(Level.INFO, "[Config] Reloading configurations...");
        try {
            for (ConfigManager manager : configManagers) {
                if (manager instanceof DatabaseConfigManager) continue;
                manager.loadConfig();
            }
            logger.log(Level.INFO, "[Config] Reload complete.");
        } catch (DatabaseException exception) {
            logger.error(exception);
        }
    }
}
