package fr.euphyllia.skyllia.api;

import fr.euphyllia.skyllia.Skyllia;
import fr.euphyllia.skyllia.api.exceptions.UnsupportedMinecraftVersionException;
import fr.euphyllia.skyllia.api.hooks.PermissionHook;
import fr.euphyllia.skyllia.api.hooks.SchematicHook;
import fr.euphyllia.skyllia.api.hooks.SpawnHook;
import fr.euphyllia.skyllia.api.service.TrustService;
import fr.euphyllia.skyllia.api.utils.nms.*;
import fr.euphyllia.skyllia.cache.SkyblockCache;
import fr.euphyllia.skyllia.configuration.ConfigLoader;
import fr.euphyllia.skyllia.database.IslandQuery;
import fr.euphyllia.skyllia.hook.HookBootstrap;
import fr.euphyllia.skyllia.hook.SkylliaSchematicHookResolver;
import fr.euphyllia.skyllia.managers.Managers;
import fr.euphyllia.skyllia.managers.skyblock.APISkyllia;
import fr.euphyllia.skyllia.managers.skyblock.SkyblockManager;
import fr.euphyllia.skyllia.managers.world.WorldModifier;
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
    private final SkylliaSchematicHookResolver schematicHookResolver;

    // World tools
    private WorldModifier worldModifier;
    // IslandQuery : lazy (DB must be initialized first)
    private volatile @Nullable IslandQuery islandQuery;
    // NMS bridges
    private WorldNMS worldNMS;
    private PlayerNMS playerNMS;
    private BiomesImpl biomesImpl;
    private ExplosionEntityImpl explosionEntityImpl;
    private MobsSpawnImpl mobsSpawnImpl;
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

        this.schematicHookResolver = new SkylliaSchematicHookResolver(this.plugin);

        loadAPI();
    }

    private void setVersionNMS() throws UnsupportedMinecraftVersionException {
        final String minecraftVersion = Bukkit.getServer().getMinecraftVersion();
        String pkg = switch (minecraftVersion) {
            case "1.20.5", "1.20.6" -> "fr.euphyllia.skyllia.utils.nms.v1_20_R4.";
            case "1.21", "1.21.1" -> "fr.euphyllia.skyllia.utils.nms.v1_21_R1.";
            case "1.21.2", "1.21.3" -> "fr.euphyllia.skyllia.utils.nms.v1_21_R2.";
            case "1.21.4" -> "fr.euphyllia.skyllia.utils.nms.v1_21_R3.";
            case "1.21.5" -> "fr.euphyllia.skyllia.utils.nms.v1_21_R4.";
            case "1.21.6", "1.21.7", "1.21.8" -> "fr.euphyllia.skyllia.utils.nms.v1_21_R5.";
            case "1.21.9", "1.21.10" -> "fr.euphyllia.skyllia.utils.nms.v1_21_R6.";
            case "1.21.11" -> "fr.euphyllia.skyllia.utils.nms.v1_21_R7.";
            case "26.1", "26.1.1", "26.1.2" -> "fr.euphyllia.skyllia.utils.nms.v26_1.";
            case "26.2" -> "fr.euphyllia.skyllia.utils.nms.v26_2.";
            default ->
                    throw new UnsupportedMinecraftVersionException("Unsupported Minecraft version: " + minecraftVersion);
        };

        try {
            this.worldNMS = (WorldNMS) Class.forName(pkg + "WorldNMS").getDeclaredConstructor().newInstance();
            this.playerNMS = (PlayerNMS) Class.forName(pkg + "PlayerNMS").getDeclaredConstructor().newInstance();
            this.biomesImpl = (BiomesImpl) Class.forName(pkg + "BiomeNMS").getDeclaredConstructor().newInstance();
            this.explosionEntityImpl = (ExplosionEntityImpl) Class.forName(pkg + "ExplosionEntityImpl").getDeclaredConstructor().newInstance();
            this.mobsSpawnImpl = (MobsSpawnImpl) Class.forName(pkg + "MobSpawnNMS").getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new UnsupportedMinecraftVersionException("Failed to load NMS classes for version " + minecraftVersion + ": " + e.getMessage());
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

    public MobsSpawnImpl getMobsSpawnImpl() {
        return this.mobsSpawnImpl;
    }

    public @NotNull WorldModifier getWorldModifier() {
        return this.worldModifier;
    }

    public @NotNull SchematicHook getSchematicHook(@NotNull fr.euphyllia.skyllia.api.skyblock.model.SchematicPlugin requested) {
        return this.schematicHookResolver.resolve(requested);
    }

    public @Nullable PermissionHook getPermissionHook() {
        return HookBootstrap.getPermissionHook();
    }

    public void initWorldModifier() {
        this.worldModifier = new WorldModifier(this.plugin);
    }

    public @Nullable SpawnHook getSpawnHook() {
        return HookBootstrap.getSpawnHook();
    }
}
