package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsInventory;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.managers.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PermissionsConfigManager implements ConfigManager {

    private static final Logger log = LogManager.getLogger(PermissionsConfigManager.class);
    private final CommentedFileConfig config;
    public ConcurrentMap<RoleType, Long> flagsRoleDefaultPermissionsCommandIsland = new ConcurrentHashMap<>();
    public ConcurrentMap<RoleType, Long> flagsRoleDefaultPermissionInventory = new ConcurrentHashMap<>();
    public ConcurrentMap<RoleType, Long> flagsRoleDefaultPermissionsIsland = new ConcurrentHashMap<>();
    private boolean changed = false;

    public PermissionsConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        changed = false;

        flagsRoleDefaultPermissionsCommandIsland.clear();
        flagsRoleDefaultPermissionInventory.clear();
        flagsRoleDefaultPermissionsIsland.clear();

        CommentedConfig islands = config.get("islands");
        CommentedConfig inventory = config.get("inventory");
        CommentedConfig commands = config.get("commands");

        if (islands == null || inventory == null || commands == null) {
            log.warn("[Skyllia] One or more permission sections (islands, inventory, commands) are missing from permissions.toml.");
            return;
        }

        for (RoleType roleType : RoleType.values()) {
            // === ISLAND PERMISSIONS ===
            CommentedConfig islandNode = islands.get(roleType.name());
            if (islandNode != null) {
                PermissionManager manager = new PermissionManager(0L);
                for (PermissionsIsland perm : PermissionsIsland.values()) {
                    String path = "islands." + roleType.name() + "." + perm.name();
                    boolean value = getOrSetDefault(path, false, Boolean.class);
                    manager.definePermission(perm.getPermissionValue(), value);
                }
                flagsRoleDefaultPermissionsIsland.put(roleType, manager.getPermissions());
            }

            // === INVENTORY PERMISSIONS ===
            CommentedConfig inventoryNode = inventory.get(roleType.name());
            if (inventoryNode != null) {
                PermissionManager manager = new PermissionManager(0L);
                for (PermissionsInventory perm : PermissionsInventory.values()) {
                    String path = "inventory." + roleType.name() + "." + perm.name();
                    boolean value = getOrSetDefault(path, false, Boolean.class);
                    manager.definePermission(perm.getPermissionValue(), value);
                }
                flagsRoleDefaultPermissionInventory.put(roleType, manager.getPermissions());
            }

            // === COMMAND PERMISSIONS ===
            CommentedConfig commandNode = commands.get(roleType.name());
            if (commandNode != null) {
                PermissionManager manager = new PermissionManager(0L);
                for (PermissionsCommandIsland perm : PermissionsCommandIsland.values()) {
                    String path = "commands." + roleType.name() + "." + perm.name();
                    boolean value = getOrSetDefault(path, false, Boolean.class);
                    manager.definePermission(perm.getPermissionValue(), value);
                }
                flagsRoleDefaultPermissionsCommandIsland.put(roleType, manager.getPermissions());
            }
        }

        if (changed) {
            config.save();
        }

        log.info("[Skyllia] Loaded permissions for {} roles.", RoleType.values().length);
    }

    @Override
    public void reloadFromDisk() {
        config.load();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrSetDefault(String path, T defaultValue, Class<T> expectedClass) {
        Object value = config.get(path);
        if (value == null) {
            config.set(path, defaultValue);
            changed = true;
            return defaultValue;
        }

        if (expectedClass.isInstance(value)) {
            return (T) value; // Bonne instance directement
        }

        // Cas spécial : Integer → Long
        if (expectedClass == Long.class && value instanceof Integer) {
            return (T) Long.valueOf((Integer) value);
        }

        // Cas spécial : Double → Float
        if (expectedClass == Float.class && value instanceof Double) {
            return (T) Float.valueOf(((Double) value).floatValue());
        }

        throw new IllegalStateException("Cannot convert value at path '" + path + "' from " + value.getClass().getSimpleName() + " to " + expectedClass.getSimpleName());
    }

    public ConcurrentMap<RoleType, Long> getPermissionInventory() {
        return flagsRoleDefaultPermissionInventory;
    }

    public ConcurrentMap<RoleType, Long> getPermissionsCommands() {
        return flagsRoleDefaultPermissionsCommandIsland;
    }

    public ConcurrentMap<RoleType, Long> getPermissionIsland() {
        return flagsRoleDefaultPermissionsIsland;
    }
}
