package fr.euphyllia.skyfolia.configuration;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class ConfigToml {
    public static FileConfig config;
    public static int version;
    private static boolean verbose;
    private static File CONFIG_FILE;
    private static final Logger LOGGER = LogManager.getLogger("fr.euphyllia.skyfolia.configuration.ConfigToml");

    public static void init(File configFile) {
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
            LOGGER.log(level, message);
        }
    }

    public static void readConfig(@NotNull Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())
                    && (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE)) {
                try {
                    method.setAccessible(true);
                    method.invoke(instance);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    log(Level.FATAL, ex.getMessage());
                }
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
}
