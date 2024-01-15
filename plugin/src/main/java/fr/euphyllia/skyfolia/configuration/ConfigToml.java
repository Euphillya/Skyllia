package fr.euphyllia.skyfolia.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.ImmutableMap;
import fr.euphyllia.skyfolia.api.skyblock.model.IslandType;
import fr.euphyllia.skyfolia.api.skyblock.model.SchematicWorld;
import fr.euphyllia.skyfolia.configuration.section.MariaDBConfig;
import fr.euphyllia.skyfolia.configuration.section.WorldConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigToml {
    private static final Logger logger = LogManager.getLogger(ConfigToml.class);
    public static CommentedFileConfig config;
    public static int version;
    public static MariaDBConfig mariaDBConfig;
    public static List<WorldConfig> worldConfigs = new ArrayList<>();
    public static int maxIsland = 100_000_000;
    public static Map<String, IslandType> islandTypes = new HashMap<>();
    public static boolean clearInventoryWhenDeleteIsland = true;
    public static boolean clearEnderChestWhenDeleteIsland = true;
    public static boolean resetExperiencePlayerWhenDeleteIsland = true;
    private static boolean verbose;

    public static void init(File configFile) {
        config = CommentedFileConfig.builder(configFile).autosave().build();
        config.load();
        verbose = getBoolean("verbose", false);

        version = getInt("config-version", 1);
        set("config-version", 1);
        logger.log(Level.FATAL, "Lecture des config");
        try {
            readConfig(ConfigToml.class, null);
        } catch (Exception e) {
            logger.log(Level.FATAL, "Erreur de lecture !", e);
        }
    }

    protected static void log(Level level, String message) {
        if (verbose) {
            logger.log(level, message);
        }
    }

    private static void readConfig(@NotNull Class<?> clazz, Object instance) throws InvocationTargetException, IllegalAccessException {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())
                    && (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE)) {
                method.setAccessible(true);
                method.invoke(instance);
            }
        }
    }

    private static void set(@NotNull String path, Object val) {
        config.set(path, val);
    }

    private static String getString(@NotNull String path, String def) {
        Object tryIt = config.get(path);
        if (tryIt == null && def != null) {
            set(path, def);
            return def;
        }
        return config.get(path);
    }

    private static Boolean getBoolean(@NotNull String path, boolean def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.get(path);
    }

    private static Double getDouble(@NotNull String path, Double def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.get(path);
    }

    private static Integer getInt(@NotNull String path, Integer def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.getInt(path);
    }

    private static <T> List getList(@NotNull String path, T def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return (List) def;
        }
        return config.get(path);
    }

    private static Long getLong(@NotNull String path, Long def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.getLong(path);
    }

    private static Map<String, ?> getMap(@NotNull String path) {
        CommentedConfig commentedConfig = config.get(path);
        return toMap(commentedConfig);
    }

    private static Map<String, ?> toMap(CommentedConfig config) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        if (config != null) {
            for (CommentedConfig.Entry entry : config.entrySet()) {
                Object obj = entry.getValue();
                if (obj != null) {
                    builder.put(entry.getKey(), obj instanceof CommentedConfig val ? toMap(val) : obj);
                }
            }
        }
        return builder.build();
    }

    private static void initMariaDB() {
        String path = "sgbd.mariadb.%s";
        String hostname = getString(path.formatted("hostname"), "127.0.0.1");
        String port = getString(path.formatted("host"), "3306");
        String username = getString(path.formatted("username"), "admin");
        String password = getString(path.formatted("password"), "azerty123@");
        boolean useSSL = getBoolean(path.formatted("useSSL"), false);
        int maxPool = getInt(path.formatted("maxPool"), 5);
        int timeOut = getInt(path.formatted("timeOut"), 500);
        String database = getString(path.formatted("database"), "sky_folia");
        mariaDBConfig = new MariaDBConfig(hostname, port, username, password, useSSL, maxPool, timeOut, database);
    }

    private static void worlds() {
        HashMap<String, ?> worldsMaps = new HashMap<>(getMap("worlds"));
        if (worldsMaps.isEmpty()) {
            worldsMaps.put("sky-overworld", null);
        }
        String parentConfig = "worlds.";
        for (Map.Entry<String, ?> entry : worldsMaps.entrySet()) {
            String key = parentConfig + entry.getKey();
            String skyblockEnvironment = getString(key + ".environment", World.Environment.NORMAL.name());
            String portalTeleport = getString(key + ".portal-tp", "sky-overworld");
            worldConfigs.add(new WorldConfig(entry.getKey(), skyblockEnvironment, portalTeleport));
        }
    }

    private static void maxIle() {
        maxIsland = getInt("config.max-island", maxIsland);
    }

    private static void typeIsland() {
        HashMap<String, ?> islandTypesHashMap = new HashMap<>(getMap("island-types"));
        String parentConfig = "island-types.";
        if (islandTypesHashMap.isEmpty()) {
            islandTypesHashMap.putIfAbsent("example", null);
        }
        for (Map.Entry<String, ?> entry : islandTypesHashMap.entrySet()) {
            String key = parentConfig + entry.getKey();
            int maxMembers = getInt(key + ".max-members", 3);
            String name = getString(key + ".name", entry.getKey());
            double rayon = getDouble(key + ".size", 50D);
            islandTypes.put(name, new IslandType(name, maxMembers, rayon));
        }
    }

    public static Map<String, SchematicWorld> schematicWorldMap = new HashMap<>();
    private static void schematicIsland() {
        HashMap<String, ?> islandStarter = new HashMap<>(getMap("island-starter"));
        String parentConfig = "island-starter.";
        if (islandStarter.isEmpty()) {
            islandStarter.putIfAbsent("example-schem", null);
        }
        for (Map.Entry<String, ?> entry : islandStarter.entrySet()) {
            String key = parentConfig + entry.getKey();
            HashMap<String, ?> worldSchem = new HashMap<>(getMap(key + ".worlds"));
            String childrenConfig = key + ".worlds.";
            if (worldSchem.isEmpty()) {
                worldSchem.put("sky-overworld", null);
            }
            for (Map.Entry<String, ?> islandStarterEntry : worldSchem.entrySet()) {
                String isKey = childrenConfig + islandStarterEntry.getKey();
                String schematicFile = getString(isKey + ".schematic", "./schematics/default.schem");
                String worldName = islandStarterEntry.getKey();
                String name = getString(isKey + ".name", entry.getKey());
                SchematicWorld schematicWorld = new SchematicWorld(name, worldName, schematicFile);
                schematicWorldMap.put(worldName, schematicWorld);
            }
        }
    }

    public static String defaultSchematicKey = "example-schem";
    private static void configIsland() {
        defaultSchematicKey = getString("island.create.default-schem-key", defaultSchematicKey);
    }

    private static void playerSettings() {
        clearInventoryWhenDeleteIsland = getBoolean("settings.player.island.delete.clear-inventory", clearInventoryWhenDeleteIsland);
        clearEnderChestWhenDeleteIsland = getBoolean("settings.player.island.delete.clear-enderchest", clearEnderChestWhenDeleteIsland);
        resetExperiencePlayerWhenDeleteIsland = getBoolean("settings.player.island.delete.clear-experience", resetExperiencePlayerWhenDeleteIsland);
    }
}
