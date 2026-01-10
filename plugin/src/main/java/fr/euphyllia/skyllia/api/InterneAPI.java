package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicPlugin;
import fr.euphyllia.skyllia.api.utils.nms.BiomesImpl;
import fr.euphyllia.skyllia.api.utils.nms.ExplosionEntityImpl;
import fr.euphyllia.skyllia.api.utils.nms.PlayerNMS;
import fr.euphyllia.skyllia.api.utils.nms.WorldNMS;
import fr.euphyllia.skyllia.api.world.WorldModifier;
import fr.euphyllia.skyllia.cache.SkyblockCache;
import fr.euphyllia.skyllia.cache.TrustService;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.database.IslandQuery;
import fr.euphyllia.skyllia.managers.Managers;
import fr.euphyllia.skyllia.managers.skyblock.APISkyllia;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.managers.world.WorldModifierSelector;
import fr.euphyllia.skyllia.sgbd.exceptions.DatabaseException;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDB;
import fr.euphyllia.skyllia.sgbd.mariadb.MariaDBLoader;
import fr.euphyllia.skyllia.sgbd.postgre.Postgres;
import fr.euphyllia.skyllia.sgbd.postgre.PostgresLoader;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLite;
import fr.euphyllia.skyllia.sgbd.sqlite.SQLiteDatabaseLoader;
import fr.euphyllia.skyllia.sgbd.utils.model.DatabaseLoader;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InterneAPI {

    private final Logger logger = LogManager.getLogger(this);

    private final Skyllia plugin;

    // Core services
    private final SkyblockCache skyblockCache;
    private final TrustService trustService;
    private final SkyblockManager skyblockManager;
    // World tools
    private final WorldModifierSelector worldSelector;
    // IslandQuery : lazy (DB must be initialized first)
    private volatile @Nullable IslandQuery islandQuery;
    // NMS bridges
    private WorldNMS worldNMS;
    private PlayerNMS playerNMS;
    private BiomesImpl biomesImpl;
    private ExplosionEntityImpl explosionEntityImpl;

    // DB + managers
    private @Nullable DatabaseLoader database;
    private Managers managers;

    public InterneAPI(Skyllia plugin) throws UnsupportedMinecraftVersionException {
        this.plugin = plugin;

        this.setVersionNMS();

        // Must be available immediately (no DB dependency)
        this.skyblockCache = new SkyblockCache();
        this.trustService = new TrustService();

        // Inject cache to avoid plugin.getInterneAPI() during boot
        this.skyblockManager = new SkyblockManager(this.plugin, this.skyblockCache);

        this.worldSelector = new WorldModifierSelector(
                this.plugin,
                Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null,
                Bukkit.getPluginManager().getPlugin("WorldEdit") != null
        );

        loadAPI();
    }

    private void setVersionNMS() throws UnsupportedMinecraftVersionException {
        final String minecraftVersion = Bukkit.getServer().getMinecraftVersion();
        switch (minecraftVersion) {
            case "1.20", "1.20.1" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R1.ExplosionEntityImpl();
            }
            case "1.20.2" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R2.ExplosionEntityImpl();
            }
            case "1.20.3", "1.20.4" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R3.ExplosionEntityImpl();
            }
            case "1.20.5", "1.20.6" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_20_R4.ExplosionEntityImpl();
            }
            case "1.21", "1.21.1" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R1.ExplosionEntityImpl();
            }
            case "1.21.2", "1.21.3" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R2.ExplosionEntityImpl();
            }
            case "1.21.4" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R3.ExplosionEntityImpl();
            }
            case "1.21.5" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R4.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R4.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R4.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R4.ExplosionEntityImpl();
            }
            case "1.21.6", "1.21.7", "1.21.8" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R5.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R5.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R5.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R5.ExplosionEntityImpl();
            }
            case "1.21.9", "1.21.10" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R6.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R6.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R6.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R6.ExplosionEntityImpl();
            }
            case "1.21.11" -> {
                this.worldNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R7.WorldNMS();
                this.playerNMS = new fr.euphyllia.skyllia.utils.nms.v1_21_R7.PlayerNMS();
                this.biomesImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R7.BiomeNMS();
                this.explosionEntityImpl = new fr.euphyllia.skyllia.utils.nms.v1_21_R7.ExplosionEntityImpl();
            }
            default ->
                    throw new UnsupportedMinecraftVersionException("Version " + minecraftVersion + " not supported!");
        }
    }

    public void createAndCopyResources(File pluginFile, String folderName) {
        File targetDir = new File(plugin.getDataFolder(), folderName);
        if (!targetDir.exists()) targetDir.mkdirs();
        copyFilesFromJarResources(pluginFile, folderName, targetDir);
    }

    private void copyFilesFromJarResources(File file, String resourceFolder, File targetFolder) {
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(resourceFolder + "/")
                        && !entry.isDirectory()
                        && !entryName.endsWith("paper-plugin.yml")) {

                    File outFile = new File(targetFolder, entryName.substring(resourceFolder.length() + 1));
                    if (!outFile.exists()) {
                        outFile.getParentFile().mkdirs();
                        try (InputStream in = plugin.getResource(entryName);
                             FileOutputStream out = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = in.read(buffer)) > 0) out.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Impossible de copier les ressources : {}", resourceFolder, e);
        }
    }

    public boolean setupSGBD() throws DatabaseException {
        if (ConfigLoader.database.getMariaDBConfig() != null) {
            MariaDB mariaDB = new MariaDB(ConfigLoader.database.getMariaDBConfig());
            this.database = new MariaDBLoader(mariaDB);
            if (!this.database.loadDatabase()) return false;
            return getIslandQuery().getDatabaseInitializeQuery().init();
        }

        if (ConfigLoader.database.getPostgreConfig() != null) {
            Postgres postgres = new Postgres(ConfigLoader.database.getPostgreConfig());
            this.database = new PostgresLoader(postgres);
            if (!this.database.loadDatabase()) return false;
            return getIslandQuery().getDatabaseInitializeQuery().init();
        }

        if (ConfigLoader.database.getSqLiteConfig() != null) {
            SQLite sqlite = new SQLite(ConfigLoader.database.getSqLiteConfig());
            this.database = new SQLiteDatabaseLoader(sqlite);
            if (!this.database.loadDatabase()) return false;
            return getIslandQuery().getDatabaseInitializeQuery().init();
        }

        return false;
    }

    private void loadAPI() {
        SkylliaAPI.setImplementation(this.plugin, new APISkyllia(this));
    }

    public Managers getManagers() {
        return managers;
    }

    public void setManagers(Managers managers) {
        this.managers = managers;
    }

    public IslandQuery getIslandQuery() {
        IslandQuery local = this.islandQuery;
        if (local != null) return local;

        // DB must exist before we build IslandQuery
        if (this.database == null) {
            throw new IllegalStateException("Database loader is not initialized yet. Call setupSGBD() after ConfigLoader.init().");
        }

        local = new IslandQuery(this);
        this.islandQuery = local;
        return local;
    }

    public Skyllia getPlugin() {
        return this.plugin;
    }

    public @Nullable DatabaseLoader getDatabaseLoader() {
        return this.database;
    }

    public SkyblockCache getSkyblockCache() {
        return this.skyblockCache;
    }

    public SkyblockManager getSkyblockManager() {
        return this.skyblockManager;
    }

    public TrustService getTrustService() {
        return this.trustService;
    }

    public @NotNull MiniMessage getMiniMessage() {
        return MiniMessage.miniMessage();
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

    public ExplosionEntityImpl getExplosionEntityImpl() {
        return this.explosionEntityImpl;
    }

    public @NotNull WorldModifier getWorldModifier(SchematicPlugin requested) {
        return this.worldSelector.resolve(requested);
    }
}
