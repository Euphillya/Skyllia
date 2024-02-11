package fr.euphyllia.skyllia.configuration;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsInventory;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.managers.skyblock.PermissionManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PermissionsToml {

    private static final Logger logger = LogManager.getLogger(PermissionsToml.class);
    public static CommentedFileConfig config;
    public static int version;
    public static ConcurrentMap<RoleType, Long> flagsRoleDefaultPermissionsCommandIsland = new ConcurrentHashMap<>();
    public static ConcurrentMap<RoleType, Long> flagsRoleDefaultPermissionInventory = new ConcurrentHashMap<>();
    public static ConcurrentMap<RoleType, Long> flagsRoleDefaultPermissionsIsland = new ConcurrentHashMap<>();
    private static boolean verbose;

    public static void init(File configFile) {
        config = CommentedFileConfig.builder(configFile).autosave().build();
        config.load();
        verbose = getBoolean("verbose", false);

        version = getInt("config-version", 1);
        set("config-version", 1);
        if (verbose) {
            logger.log(Level.INFO, "Lecture des config");
        }
        try {
            readConfig(PermissionsToml.class, null);
        } catch (Exception e) {
            logger.log(Level.FATAL, "Erreur de lecture !", e);
        }
    }

    private static Integer getInt(@NotNull String path, Integer def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.getInt(path);
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

    private static void remove(@NotNull String path) {
        config.remove(path);
    }

    private static Boolean getBoolean(@NotNull String path, boolean def) {
        Object tryIt = config.get(path);
        if (tryIt == null) {
            set(path, def);
            return def;
        }
        return config.get(path);
    }

    private static void defaultPermissionsCommandIsland() {
        for (RoleType roleType : RoleType.values()) {
            boolean isOwner = roleType.equals(RoleType.OWNER) || roleType.equals(RoleType.CO_OWNER);
            for (PermissionsCommandIsland permissionsCommandIsland : PermissionsCommandIsland.values()) {
                boolean permissionsValue = getBoolean("commands.%s.%s".formatted(roleType.name(), permissionsCommandIsland.name()), isOwner);
                PermissionManager permissionManager = new PermissionManager(flagsRoleDefaultPermissionsCommandIsland.getOrDefault(roleType, 0L));
                permissionManager.definePermission(permissionsCommandIsland.getPermissionValue(), permissionsValue);
                flagsRoleDefaultPermissionsCommandIsland.put(roleType, permissionManager.getPermissions());
            }
            for (PermissionsInventory permissionsCommandIsland : PermissionsInventory.values()) {
                boolean permissionsValue = getBoolean("inventory.%s.%s".formatted(roleType.name(), permissionsCommandIsland.name()), isOwner);
                PermissionManager permissionManager = new PermissionManager(flagsRoleDefaultPermissionInventory.getOrDefault(roleType, 0L));
                permissionManager.definePermission(permissionsCommandIsland.getPermissionValue(), permissionsValue);
                flagsRoleDefaultPermissionInventory.put(roleType, permissionManager.getPermissions());
            }
            for (PermissionsIsland permissionsCommandIsland : PermissionsIsland.values()) {
                boolean permissionsValue = getBoolean("islands.%s.%s".formatted(roleType.name(), permissionsCommandIsland.name()), isOwner);
                PermissionManager permissionManager = new PermissionManager(flagsRoleDefaultPermissionsIsland.getOrDefault(roleType, 0L));
                permissionManager.definePermission(permissionsCommandIsland.getPermissionValue(), permissionsValue);
                flagsRoleDefaultPermissionsIsland.put(roleType, permissionManager.getPermissions());
            }
        }
    }
}
