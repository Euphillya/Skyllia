package fr.euphyllia.skyllia.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.ImmutableMap;
import fr.euphyllia.skyllia.api.configuration.PortalConfig;
import fr.euphyllia.skyllia.api.configuration.WorldConfig;
import fr.euphyllia.skyllia.api.skyblock.model.IslandSettings;
import fr.euphyllia.skyllia.api.skyblock.model.SchematicSetting;
import fr.euphyllia.skyllia.sgbd.configuration.MariaDBConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OldConfigToml {
    private static final Logger logger = LogManager.getLogger(OldConfigToml.class);
    public static CommentedFileConfig config;
    public static int version;
    public static MariaDBConfig mariaDBConfig;
    public static List<WorldConfig> worldConfigs = new ArrayList<>();
    public static int maxIsland = 500_000;
    public static ConcurrentHashMap<String, IslandSettings> islandSettingsMap = new ConcurrentHashMap<>();
    public static boolean clearInventoryWhenDeleteIsland = true;
    public static boolean clearEnderChestWhenDeleteIsland = true;
    public static boolean resetExperiencePlayerWhenDeleteIsland = true;
    public static boolean clearInventoryWhenKickedIsland = true;
    public static boolean clearEnderChestWhenKickedIsland = true;
    public static boolean resetExperiencePlayerWhenKickedIsland = true;
    public static boolean clearInventoryWhenLeaveIsland = true;
    public static boolean clearEnderChestWhenLeaveIsland = true;
    public static boolean resetExperiencePlayerWhenLeaveIsland = true;
    public static ConcurrentHashMap<String, ConcurrentHashMap<String, SchematicSetting>> schematicWorldMap = new ConcurrentHashMap<>();
    public static String defaultSchematicKey = "example-schem";
    public static int updateCacheTimer = 60;
    public static int regionDistance = -1;
    public static boolean deleteChunkPerimeterIsland = false;
    public static boolean suppressWarningNetherEndEnabled = false;
    public static boolean teleportPlayerOnIslandWhenJoin = true;
    public static boolean teleportPlayerNotIslandWhenJoin = true;
    public static boolean changeGameModeWhenTeleportIsland = true;
    public static @Nullable Location spawnWorld = null;
    public static boolean preventDeletionIfHasMembers = false;
    public static boolean debug_permission = false;
    private static boolean verbose;

    public static void init(File configFile) {
        config = CommentedFileConfig.builder(configFile).sync().autosave().build();
        config.load();
        verbose = getBoolean("verbose", false);

        version = getInt("config-version", 3);
        set("config-version", 3);
        if (verbose) {
            logger.log(Level.INFO, "Reading configurations");
        }
        try {
            readConfig(OldConfigToml.class, null);
        } catch (Exception e) {
            logger.log(Level.FATAL, "Reading error!", e);
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

}
