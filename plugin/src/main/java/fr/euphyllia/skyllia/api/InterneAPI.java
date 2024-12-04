package fr.euphyllia.skyllia.api;

import fr.euphyllia.sgbd.DatabaseLoader;
import fr.euphyllia.sgbd.MariaDB;
import fr.euphyllia.sgbd.exceptions.DatabaseException;
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
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class InterneAPI {

    private final Logger logger = LogManager.getLogger(this);
    private final Main plugin;
    private final SkyblockManager skyblockManager;
    private final CacheManager cacheManager;
    private @Nullable DatabaseLoader database;
    private DatabaseLoader databaseLoader;
    private Managers managers;
    private WorldNMS worldNMS;
    private PlayerNMS playerNMS;
    private BiomesImpl biomesImpl;

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

    public boolean setupConfigs(File dataFolder, String fileName, ConfigInitializer initializer) throws IOException {
        File configFile = this.checkFileExist(dataFolder, fileName);
        if (configFile == null) {
            return false;
        }

        try {
            initializer.initConfig(configFile);
        } catch (Exception ex) {
            logger.log(Level.FATAL, ex.getMessage(), ex);
            return false;
        }
        return true;
    }

    public void setupFirstSchematic(@NotNull File dataFolder, @Nullable InputStream resource) {
        File schematicsDir = new File(dataFolder, "schematics");
        File defaultSchem = new File(schematicsDir, "default.schem");
        if (!schematicsDir.exists()) {
            schematicsDir.mkdirs();
        } else {
            return;
        }
        if (!defaultSchem.exists()) {
            try {
                try (InputStream in = resource) {
                    if (in != null) {
                        Files.copy(in, defaultSchem.toPath());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    public IslandQuery getIslandQuery() {
        return new IslandQuery(this, ConfigToml.mariaDBConfig.database());
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
            case "1.20", "1.20.1" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.PlayerNMS();
                biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.BiomeNMS();
            }
            case "1.20.2" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.PlayerNMS();
                biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.BiomeNMS();
            }
            case "1.20.3", "1.20.4" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.PlayerNMS();
                biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.BiomeNMS();
            }
            case "1.20.5", "1.20.6" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.PlayerNMS();
                biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.BiomeNMS();
            }
            case "1.21", "1.21.1" -> {
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.PlayerNMS();
                biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.BiomeNMS();
            }
            case "1.21.2", "1.21.3" -> {
                logger.warn("Warning: Version 1.21.3 is not fully tested. Be cautious!");
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.PlayerNMS();
                biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.BiomeNMS();
            }
            case "1.21.4" -> {
                logger.warn("Warning: Version 1.21.4 is not fully tested. Be cautious!");
                worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.WorldNMS();
                playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.PlayerNMS();
                biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.BiomeNMS();
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

    public BiomesImpl getBiomesImpl() {
        return this.biomesImpl;
    }
}
