package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.database.DatabaseLoader;
import fr.euphyllia.skyllia.api.database.sgbd.MariaDB;
import fr.euphyllia.skyllia.api.exceptions.DatabaseException;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.utils.nms.PlayerNMS;
import fr.euphyllia.skyllia.api.utils.nms.WorldNMS;
import fr.euphyllia.skyllia.cache.CacheManager;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.configuration.PermissionsToml;
import fr.euphyllia.skyllia.database.mariadb.MariaDBCreateTable;
import fr.euphyllia.skyllia.database.mariadb.MariaDBTransactionQuery;
import fr.euphyllia.skyllia.database.IslandQuery;
import fr.euphyllia.skyllia.managers.Managers;
import fr.euphyllia.skyllia.managers.skyblock.APISkyllia;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class InterneAPI {

    private final Logger logger = LogManager.getLogger(this);
    ;
    private final Main plugin;
    private final SkyblockManager skyblockManager;
    private final CacheManager cacheManager;
    private @Nullable DatabaseLoader database;
    private MariaDBTransactionQuery transaction;
    private DatabaseLoader databaseLoader;
    private Managers managers;
    private WorldNMS worldNMS;
    private PlayerNMS playerNMS;

    public InterneAPI(Main plugin) throws UnsupportedMinecraftVersionException {
        this.plugin = plugin;
        this.setVersionNMS();
        this.skyblockManager = new SkyblockManager(this.plugin);
        this.cacheManager = new CacheManager(this.skyblockManager, this);
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

    private @Nullable File checkFileExist(File dataFolder, String fileName) throws IOException {
        if (!dataFolder.exists() && (!dataFolder.mkdir())) {
            logger.log(Level.FATAL, "Unable to create the configuration folder.");
            return null;

        }
        FileSystem fs = FileSystems.getDefault();
        Path configFolder = fs.getPath(dataFolder.getAbsolutePath());

        File configFile = new File(configFolder + File.separator + fileName);
        if (!configFile.exists()) {
            configFile.createNewFile();
        }
        return configFile;
    }

    public boolean setupConfigs(File dataFolder, String fileName) throws IOException {
        File configFile = this.checkFileExist(dataFolder, fileName);
        if (configFile == null) {
            return false;
        }

        try {
            ConfigToml.init(configFile);
        } catch (Exception ex) {
            logger.log(Level.FATAL, ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    public boolean setupConfigPermissions(File dataFolder, String fileName) throws IOException {
        File configFile = this.checkFileExist(dataFolder, fileName);
        if (configFile == null) {
            return false;
        }
        try {
            PermissionsToml.init(configFile);
        } catch (Exception ex) {
            logger.log(Level.FATAL, ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    public boolean setupConfigLanguage(File dataFolder, String fileName) throws IOException {
        File configFile = this.checkFileExist(dataFolder, fileName);
        if (configFile == null) {
            return false;
        }

        try {
            LanguageToml.init(configFile);
        } catch (Exception ex) {
            logger.log(Level.FATAL, ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    public boolean setupSGBD(File dataFolder) throws DatabaseException {
        if (ConfigToml.mariaDBConfig != null) {
            MariaDB mariaDB = new MariaDB(dataFolder.getAbsolutePath(), ConfigToml.mariaDBConfig);
            this.database = new DatabaseLoader(mariaDB);
            if (!this.database.loadDatabase()) {
                return false;
            }
            boolean start = new MariaDBCreateTable(this).init();
            this.transaction = new MariaDBTransactionQuery();
            return start;
        } else {
            return false;
        }
    }

    public IslandQuery getIslandQuery() {
        return new IslandQuery(this, this.transaction.getDatabaseName());
    }

    public Main getPlugin() {
        return this.plugin;
    }

    public SkyblockManager getSkyblockManager() {
        return this.skyblockManager;
    }

    public @NotNull MiniMessage getMiniMessage() {
        return MiniMessage.miniMessage();
    }

    public void updateCache(Player player) {
        this.cacheManager.updateCache(skyblockManager, player);
    }

    public CacheManager getCacheManager() {
        return this.cacheManager;
    }

    private void setVersionNMS() throws UnsupportedMinecraftVersionException {
        final String[] bukkitVersion = Bukkit.getServer().getBukkitVersion().split("-");
        switch (bukkitVersion[0]) {
            case "1.17", "1.17.1" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_17_R1.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_17_R1.PlayerNMS();
            }
            case "1.18", "1.18.1" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_18_R1.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_18_R1.PlayerNMS();
            }
            case "1.18.2" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_18_R2.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_18_R2.PlayerNMS();
            }
            case "1.19.4" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_19_R3.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_19_R3.PlayerNMS();
            }
            case "1.20", "1.20.1" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.PlayerNMS();
            }
            case "1.20.2" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.PlayerNMS();
            }
            case "1.20.3", "1.20.4" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.PlayerNMS();
            }
            default ->
                    throw new UnsupportedMinecraftVersionException("Version %s not supported !".formatted(bukkitVersion[0]));
        }
    }

    public WorldNMS getWorldNMS() {
        return this.worldNMS;
    }

    public PlayerNMS getPlayerNMS() {
        return this.playerNMS;
    }

    public void loadAPI() {
        SkylliaAPI.setImplementation(this.plugin, new APISkyllia(this));
    }
}
