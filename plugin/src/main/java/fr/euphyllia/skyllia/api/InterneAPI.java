package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.Main;
import fr.euphyllia.skyllia.api.database.DatabaseLoader;
import fr.euphyllia.skyllia.api.database.sgbd.MariaDB;
import fr.euphyllia.skyllia.api.exceptions.DatabaseException;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.utils.nms.PlayerNMS;
import fr.euphyllia.skyllia.api.utils.nms.WorldNMS;
import fr.euphyllia.skyllia.api.utils.scheduler.SchedulerTask;
import fr.euphyllia.skyllia.cache.CacheManager;
import fr.euphyllia.skyllia.configuration.ConfigToml;
import fr.euphyllia.skyllia.configuration.LanguageToml;
import fr.euphyllia.skyllia.configuration.PermissionsToml;
import fr.euphyllia.skyllia.database.query.MariaDBCreateTable;
import fr.euphyllia.skyllia.database.query.MariaDBTransactionQuery;
import fr.euphyllia.skyllia.database.query.exec.IslandQuery;
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
    private final SchedulerTask schedulerTask;
    private @Nullable DatabaseLoader database;
    private MariaDBTransactionQuery transaction;
    private DatabaseLoader databaseLoader;
    private Managers managers;
    private boolean useFolia = false;
    private WorldNMS worldNMS;
    private PlayerNMS playerNMS;

    public InterneAPI(Main plugin) throws UnsupportedMinecraftVersionException {
        this.plugin = plugin;
        this.schedulerTask = new SchedulerTask(plugin);
        this.setVersionNMS();
        this.skyblockManager = new SkyblockManager(this.plugin);
        this.cacheManager = new CacheManager(this.skyblockManager);
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            this.useFolia = true;
        } catch (ClassNotFoundException ignored) {
            this.useFolia = false;
        }
    }

    public boolean isFolia() {
        return this.useFolia;
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

    public boolean setupSGBD() throws DatabaseException {
        if (ConfigToml.mariaDBConfig != null) {
            MariaDB mariaDB = new MariaDB(ConfigToml.mariaDBConfig);
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
        final String versionMC = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        switch (versionMC) {
            case "v1_19_R3" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_19_R3.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_19_R3.PlayerNMS();
            }
            case "v1_20_R1" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.PlayerNMS();
            }
            case "v1_20_R2" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.PlayerNMS();
            }
            case "v1_20_R3" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.PlayerNMS();
            }
            default ->
                    throw new UnsupportedMinecraftVersionException("Version %s not supported !".formatted(versionMC));
        }
    }

    public WorldNMS getWorldNMS() {
        return this.worldNMS;
    }

    public PlayerNMS getPlayerNMS() {
        return this.playerNMS;
    }

    public void loadAPI() {
        SkylliaAPI.setImplementation(new APISkyllia(this));
    }

    public SchedulerTask getSchedulerTask() {
        return this.schedulerTask;
    }
}
