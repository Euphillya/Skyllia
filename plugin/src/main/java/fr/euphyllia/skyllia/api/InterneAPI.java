package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.configuration.ConfigInitializer;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import fr.euphyllia.skyllia.api.utils.nms.PlayerNMS;
import fr.euphyllia.skyllia.api.utils.nms.WorldNMS;
import fr.euphyllia.skyllia.cache.CacheManager;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.database.IslandQuery;
import fr.euphyllia.skyllia.managers.Managers;
import fr.euphyllia.skyllia.managers.skyblock.APISkyllia;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.sgbd.DatabaseLoader;
import fr.euphyllia.skyllia.sgbd.MariaDB;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Internal API class, managing NMS, database connections, and caching.
 */
public class InterneAPI {

    private static final Logger LOGGER = LogManager.getLogger(InterneAPI.class);

    private final Main plugin;
    private final SkyblockManager skyblockManager;
    private final CacheManager cacheManager;

    private WorldNMS worldNMS;
    private PlayerNMS playerNMS;
    private BiomesImpl biomesImpl;

    private DatabaseLoader database;
    private Managers managers;

    /**
     * Constructor that checks the Minecraft version and initializes NMS classes.
     *
     * @param plugin The main plugin instance
     * @throws UnsupportedMinecraftVersionException if server version is not supported
     */
    public InterneAPI(Main plugin) throws UnsupportedMinecraftVersionException {
        this.plugin = plugin;
        setVersionNMS();
        this.skyblockManager = new SkyblockManager(plugin);
        this.cacheManager = new CacheManager(this.skyblockManager, this);
        loadAPI();
    }

    /**
     * Determines the version of NMS to use based on the Bukkit server version.
     *
     * @throws UnsupportedMinecraftVersionException if the version is not supported
     */
    private void setVersionNMS() throws UnsupportedMinecraftVersionException {
        final String[] bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-");
        switch (bukkitVersion[0]) {
            case "1.20", "1.20.1" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.BiomeNMS();
            }
            case "1.20.2" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.BiomeNMS();
            }
            case "1.20.3", "1.20.4" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.BiomeNMS();
            }
            case "1.20.5", "1.20.6" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.BiomeNMS();
            }
            case "1.21", "1.21.1" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.BiomeNMS();
            }
            case "1.21.2", "1.21.3" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.BiomeNMS();
            }
            case "1.21.4" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.BiomeNMS();
            }
            default -> {
                throw new UnsupportedMinecraftVersionException("Version " + bukkitVersion[0] + " not supported!");
            }
        }
    }


    /**
     * Loads a config file if it doesn't exist, then initializes it via a ConfigInitializer.
     *
     * @param dataFolder Path to the plugin's data folder
     * @param fileName   The config file name
     * @param initializer The ConfigInitializer functional interface
     * @return true if successful, false otherwise
     * @throws IOException if file creation fails
     */
    public boolean setupConfigs(File dataFolder, String fileName, ConfigInitializer initializer) throws IOException {
        File configFile = checkFileExist(dataFolder, fileName);
        if (configFile == null) {
            return false;
        }
        try {
            initializer.initConfig(configFile);
        } catch (Exception ex) {
            LOGGER.error("Error while initializing config: ", ex);
            return false;
        }
        return true;
    }

    /**
     * Copies a default schematic if it does not exist already.
     *
     * @param dataFolder The plugin's data folder
     * @param resource   The InputStream for the default schematic
     */
    public void setupFirstSchematic(@NotNull File dataFolder, @Nullable InputStream resource) {
        File schematicsDir = new File(dataFolder, "schematics");
        File defaultSchem = new File(schematicsDir, "default.schem");
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
        } else {
            return;
        }
        if (!defaultSchem.exists() && resource != null) {
            try (InputStream in = resource) {
                Files.copy(in, defaultSchem.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes the SGBD (e.g., MariaDB) from the config, creating the database schema if necessary.
     *
     * @return true if successful, false otherwise
     * @throws DatabaseException if database initialization fails
     */
    public boolean setupSGBD() throws DatabaseException {
        if (ConfigToml.mariaDBConfig != null) {
            MariaDB mariaDB = new MariaDB(ConfigToml.mariaDBConfig);
            this.database = new DatabaseLoader(mariaDB);
            if (!this.database.loadDatabase()) {
                return false;
            }
            return getIslandQuery().getDatabaseInitializeQuery().init();
        } else {
            return false;
        }
    }

    /**
     * Returns an IslandQuery instance to access or modify island data.
     *
     * @return A new IslandQuery instance
     */
    public IslandQuery getIslandQuery() {
        return new IslandQuery(this, ConfigToml.mariaDBConfig.database());
    }

    /**
     * Periodically updates the cache for a given player.
     *
     * @param player The player whose cache is updated
     */
    public void updateCache(Player player) {
        this.cacheManager.updateCache(skyblockManager, player);
    }

    /**
     * Loads the public API implementation.
     */
    private void loadAPI() {
        fr.euphyllia.skyllia.api.SkylliaAPI.setImplementation(this.plugin, new APISkyllia(this));
    }

    /**
     * Ensures a config file exists in the dataFolder, creating it if necessary.
     *
     * @param dataFolder The plugin data folder
     * @param fileName   The file name
     * @return The File object or null if creation fails
     * @throws IOException if file creation fails
     */
    private @Nullable File checkFileExist(File dataFolder, String fileName) throws IOException {
        if (!dataFolder.exists() && !dataFolder.mkdir()) {
            LOGGER.error("Could not create directory: {}", dataFolder.getAbsolutePath());
            return null;
        }
        Path configFolder = FileSystems.getDefault().getPath(dataFolder.getAbsolutePath());
        File configFile = new File(configFolder + File.separator + fileName);
        if (!configFile.exists()) {
            configFile.createNewFile();
        }
        return configFile;
    }

    /* Getters and setters */

    public Main getPlugin() {
        return this.plugin;
    }

    public Managers getManagers() {
        return managers;
    }

    public void setManagers(Managers managers) {
        this.managers = managers;
    }

    public DatabaseLoader getDatabaseLoader() {
        return this.database;
    }

    public SkyblockManager getSkyblockManager() {
        return this.skyblockManager;
    }

    public @NotNull MiniMessage getMiniMessage() {
        return MiniMessage.miniMessage();
    }

    public CacheManager getCacheManager() {
        return this.cacheManager;
    }

    public WorldNMS getWorldNMS() {
        return this.worldNMS;
    }

    public PlayerNMS getPlayerNMS() {
        return this.playerNMS;
    }

    public BiomesImpl getBiomesImpl() {
        return this.biomesImpl;
    }
}
