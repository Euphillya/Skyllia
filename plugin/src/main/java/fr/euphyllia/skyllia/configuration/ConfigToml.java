package fr.euphyllia.skyllia.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.ImmutableMap;
import fr.euphyllia.skyllia.api.configuration.MariaDBConfig;
import fr.euphyllia.skyllia.api.configuration.PortalConfig;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.configuration.model.MariaDB;
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
    public static Map<String, IslandSettings> islandSettingsMap = new HashMap<>();
    public static boolean clearInventoryWhenDeleteIsland = true;
    public static boolean clearEnderChestWhenDeleteIsland = true;
    public static boolean resetExperiencePlayerWhenDeleteIsland = true;
    public static Map<String, Map<String, SchematicSetting>> schematicWorldMap = new HashMap<>();
    public static String defaultSchematicKey = "example-schem";
    public static int updateCacheTimer = 60;
    public static int regionDistance = -1;
    private static boolean verbose;

    public static void init(File configFile) {
        config = CommentedFileConfig.builder(configFile).autosave().build();
        config.load();
        verbose = getBoolean("verbose", false);

        version = getInt("config-version", 3);
        set("config-version", 3);
        if (verbose) {
            logger.log(Level.INFO, "Lecture des config");
        }
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

    protected static void remove(@NotNull String path) {
        config.remove(path);
    }

    protected static String getString(@NotNull String path, String def) {
        Object tryIt = config.get(path);
        if (tryIt == null && def != null) {
            set(path, def);
            return def;
        }
        return config.get(path);
    }

    protected static Boolean getBoolean(@NotNull String path, boolean def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.get(path);
    }

    protected static Double getDouble(@NotNull String path, Double def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        if (tryIt instanceof Double) {
            return config.get(path);
        } else if (tryIt instanceof Integer) {
            return (double) config.getInt(path);
        } else {
            String value = String.valueOf(config.get(path));
            return Double.parseDouble(value);
        }

    }

    protected static Integer getInt(@NotNull String path, Integer def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.getInt(path);
    }

    protected static <T> List getList(@NotNull String path, T def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return (List) def;
        }
        return config.get(path);
    }

    protected static Long getLong(@NotNull String path, Long def) {
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
        mariaDBConfig = new MariaDB().getConstructor();
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
            if (version < 2) {
                String oldValue = getString(key + ".nether-portal", "sky-overworld");
                set(key + ".portal-nether.direction", oldValue);
                remove(key + ".nether-portal");
                oldValue = getString(key + ".end-portal-tp", "sky-overworld");
                set(key + ".portal-end.direction", oldValue);
                remove(key + ".end-portal-tp");
            }
            String portalNetherDirection = getString(key + ".portal-nether.direction", "sky-overworld");
            boolean portalNetherEnabled = getBoolean(key + ".portal-nether.enabled", true);
            String portalEndDirection = getString(key + ".portal-end.direction", "sky-overworld");
            boolean portalEndEnabled = getBoolean(key + ".portal-end.enabled", true);
            worldConfigs.add(new WorldConfig(entry.getKey(), skyblockEnvironment,
                    new PortalConfig(portalNetherEnabled, portalNetherDirection),
                    new PortalConfig(portalEndEnabled, portalEndDirection)));
        }
    }

    private static void configs() {
        maxIsland = getInt("config.max-island", maxIsland);
        regionDistance = getInt("config.region-distance-per-island", regionDistance);
    }

    private static void typeIsland() {
        HashMap<String, ?> islandSettingsMaps = new HashMap<>(getMap("island-settings"));
        String settingsParent = "island-settings.";
        if (islandSettingsMaps.isEmpty()) {
            islandSettingsMaps.putIfAbsent("example", null);
        }
        if (version < 3) {
            HashMap<String, ?> islandTypesHashMap = new HashMap<>(getMap("island-types"));
            String parentConfig = "island-types.";
            if (islandTypesHashMap.isEmpty()) {
                islandTypesHashMap.putIfAbsent("example", null);
            }
            for (Map.Entry<String, ?> entry : islandTypesHashMap.entrySet()) {
                String key = parentConfig + entry.getKey();

                int maxMembersOV = getInt(key + ".max-members", 3);
                set(settingsParent + entry.getKey() + ".max-members", maxMembersOV);

                String nameOV = getString(key + ".name", entry.getKey());
                set(settingsParent + entry.getKey() + ".name", nameOV);

                double rayonOV = getDouble(key + ".size", 50D);
                set(settingsParent + entry.getKey() + ".size", rayonOV);
            }
            remove("island-types");
        }

        for (Map.Entry<String, ?> entry : islandSettingsMaps.entrySet()) {
            String key = settingsParent + entry.getKey();
            int maxMembers = getInt(key + ".max-members", 3);
            String name = getString(key + ".name", entry.getKey());
            double rayon = getDouble(key + ".size", 50D);
            islandSettingsMap.put(name, new IslandSettings(name, maxMembers, rayon));
        }
    }

    private static void schematicIsland() {
        HashMap<String, ?> islandStarter = new HashMap<>(getMap("island-starter"));
        String parentConfig = "island-starter.";
        if (islandStarter.isEmpty()) {
            islandStarter.putIfAbsent("example-schem", null);
        }
        for (Map.Entry<String, ?> entry : islandStarter.entrySet()) {
            String namekey = entry.getKey();
            String key = parentConfig + namekey;
            HashMap<String, ?> worldSchem = new HashMap<>(getMap(key + ".worlds"));
            String childrenConfig = key + ".worlds.";
            if (worldSchem.isEmpty()) {
                worldSchem.put("sky-overworld", null);
            }

            for (Map.Entry<String, ?> islandStarterEntry : worldSchem.entrySet()) {
                Map<String, SchematicSetting> keySchematicsByName = schematicWorldMap.getOrDefault(namekey, new HashMap<>()); // Nom du monde - Schematic
                String worldName = islandStarterEntry.getKey();
                String isKey = childrenConfig + worldName;
                String schematicFile = getString(isKey + ".schematic", "./schematics/default.schem");
                double height = getDouble(isKey + ".height", 64D);
                SchematicSetting schematicSetting = new SchematicSetting(height, schematicFile);
                if (keySchematicsByName.get(worldName) != null) {
                    log(Level.ERROR, "Your %s type has the same world twice: %s ! It will be ignored!".formatted(namekey, worldName));
                    continue;
                }
                keySchematicsByName.put(worldName, schematicSetting);
                schematicWorldMap.put(namekey, keySchematicsByName);
            }
        }
    }

    private static void configIsland() {
        defaultSchematicKey = getString("island.create.default-schem-key", defaultSchematicKey);
    }

    private static void playerSettings() {
        clearInventoryWhenDeleteIsland = getBoolean("settings.player.island.delete.clear-inventory", clearInventoryWhenDeleteIsland);
        clearEnderChestWhenDeleteIsland = getBoolean("settings.player.island.delete.clear-enderchest", clearEnderChestWhenDeleteIsland);
        resetExperiencePlayerWhenDeleteIsland = getBoolean("settings.player.island.delete.clear-experience", resetExperiencePlayerWhenDeleteIsland);
    }

    private static void updateCache() {
        updateCacheTimer = getInt("settings.global.cache.update-timer-seconds", updateCacheTimer);
    }
}
