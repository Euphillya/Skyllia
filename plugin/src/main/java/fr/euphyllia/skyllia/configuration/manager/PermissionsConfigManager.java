package fr.euphyllia.skyllia.configuration.manager;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import fr.euphyllia.skyllia.api.skyblock.PermissionManager;
import fr.euphyllia.skyllia.api.skyblock.model.RoleType;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsCommandIsland;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsInventory;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.managers.ConfigManager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PermissionsConfigManager implements ConfigManager {

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
        CommentedConfig commands = config.get("inventory");

        if (islands == null || inventory == null || commands == null) return;

        for (RoleType roleType : RoleType.values()) {
            boolean isOwner = roleType.equals(RoleType.OWNER) || roleType.equals(RoleType.CO_OWNER);
            for (PermissionsCommandIsland permissionsCommandIsland : PermissionsCommandIsland.values()) {
                CommentedConfig node = islands.get(roleType.name());
                if (node == null) continue;

                boolean permissionsValue = node.getOrElse(permissionsCommandIsland.name(), false);
                PermissionManager permissionManager = new PermissionManager(flagsRoleDefaultPermissionsCommandIsland.getOrDefault(roleType, 0L));
                permissionManager.definePermission(permissionsCommandIsland.getPermissionValue(), permissionsValue);
                flagsRoleDefaultPermissionsCommandIsland.put(roleType, permissionManager.getPermissions());
            }
            for (PermissionsInventory permissionsInventory : PermissionsInventory.values()) {
                CommentedConfig node = inventory.get(roleType.name());
                if (node == null) continue;

                boolean permissionsValue = node.getOrElse(permissionsInventory.name(), false);
                PermissionManager permissionManager = new PermissionManager(flagsRoleDefaultPermissionsCommandIsland.getOrDefault(roleType, 0L));
                permissionManager.definePermission(permissionsInventory.getPermissionValue(), permissionsValue);
                flagsRoleDefaultPermissionsCommandIsland.put(roleType, permissionManager.getPermissions());
            }
            for (PermissionsIsland permissionsIsland : PermissionsIsland.values()) {
                CommentedConfig node = commands.get(roleType.name());
                if (node == null) continue;

                boolean permissionsValue = node.getOrElse(permissionsIsland.name(), false);
                PermissionManager permissionManager = new PermissionManager(flagsRoleDefaultPermissionsCommandIsland.getOrDefault(roleType, 0L));
                permissionManager.definePermission(permissionsIsland.getPermissionValue(), permissionsValue);
                flagsRoleDefaultPermissionsCommandIsland.put(roleType, permissionManager.getPermissions());
            }
        }
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
