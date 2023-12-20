package fr.euphyllia.skyfolia.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.common.collect.ImmutableMap;
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
    public static FileConfig config;
    public static int version;
    private static boolean verbose;
    private static File CONFIG_FILE;
    private static Logger logger;

    public static void init(File configFile) throws Exception {
        logger = LogManager.getLogger("fr.euphyllia.skyfolia.configuration.ConfigToml.%s".formatted(configFile.getName()));
        CONFIG_FILE = configFile;
        config = FileConfig.of(configFile);
        config.load();
        verbose = getBoolean("verbose", false);

        version = getInt("config-version", 1);
        set("config-version", 1);

        readConfig(ConfigToml.class, null);
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

        config.save();
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

    private static Map<String, ?> getMap(@NotNull String path, Object def) {
        CommentedConfig commentedConfig = (CommentedConfig) config.getOrElse(path, def);
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

    public static MariaDBConfig mariaDBConfig;

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

    public static List<WorldConfig> worldConfigs = new ArrayList<>();

    private static void worlds() {
        Map<String, ?> worldsMaps = getMap("worlds", new HashMap<>());
        String parentConfig = "worlds.";
        for (Map.Entry<String, ?> entry : worldsMaps.entrySet()) {
            String key = parentConfig + entry.getKey();
            String skyblockEnvironment = getString(key+ ".environment", World.Environment.NORMAL.name());
            worldConfigs.add(new WorldConfig(entry.getKey(), skyblockEnvironment));
        }
    }

    public static int maxIsland = 100_000_000;
    private static void maxIle() {
        maxIsland = getInt("config.maxIsland", maxIsland);
    }
}
