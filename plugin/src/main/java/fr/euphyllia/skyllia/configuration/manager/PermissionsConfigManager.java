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
    public ConcurrentMap<RoleType, Long> flagsRoleDefaultPermissionsCommandIsland = new ConcurrentHashMap<>();
    public ConcurrentMap<RoleType, Long> flagsRoleDefaultPermissionInventory = new ConcurrentHashMap<>();
    public ConcurrentMap<RoleType, Long> flagsRoleDefaultPermissionsIsland = new ConcurrentHashMap<>();
    private final CommentedFileConfig config;

    public PermissionsConfigManager(CommentedFileConfig config) {
        this.config = config;
        loadConfig();
    }

    @Override
    public void loadConfig() {

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
                    boolean value = islandNode.getOrElse(perm.name(), false);
                    manager.definePermission(perm.getPermissionValue(), value);
                }
                flagsRoleDefaultPermissionsIsland.put(roleType, manager.getPermissions());
            }

            // === INVENTORY PERMISSIONS ===
            CommentedConfig inventoryNode = inventory.get(roleType.name());
            if (inventoryNode != null) {
                PermissionManager manager = new PermissionManager(0L);
                for (PermissionsInventory perm : PermissionsInventory.values()) {
                    boolean value = inventoryNode.getOrElse(perm.name(), false);
                    manager.definePermission(perm.getPermissionValue(), value);
                }
                flagsRoleDefaultPermissionInventory.put(roleType, manager.getPermissions());
            }

            // === COMMAND PERMISSIONS ===
            CommentedConfig commandNode = commands.get(roleType.name());
            if (commandNode != null) {
                PermissionManager manager = new PermissionManager(0L);
                for (PermissionsCommandIsland perm : PermissionsCommandIsland.values()) {
                    boolean value = commandNode.getOrElse(perm.name(), false);
                    manager.definePermission(perm.getPermissionValue(), value);
                }
                flagsRoleDefaultPermissionsCommandIsland.put(roleType, manager.getPermissions());
            }
        }
        log.info("[Skyllia] Loaded permissions for {} roles.", RoleType.values().length);
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
