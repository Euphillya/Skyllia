package fr.euphyllia.skyfolia.managers.skyblock;

import fr.euphyllia.skyfolia.api.skyblock.model.permissions.Permissions;
import fr.euphyllia.skyfolia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyfolia.api.utils.BitwiseFlag;

public class PermissionManager extends BitwiseFlag {

    public PermissionManager(long actualFlags) {
        this.flags = actualFlags;
    }

    public static long valueOf(String permissions) {
        return PermissionsIsland.permissionValue(permissions);
    }

    public long getPermissions() {
        return this.flags;
    }

    public void definePermission(long permission, boolean value) {
        this.setFlags(permission, value);
    }

    public boolean hasPermission(long permission) {
        return this.isFlagSet(permission);
    }

    public boolean hasPermission(Permissions permissionsIsland) {
        return this.isFlagSet(permissionsIsland.getPermissionValue());
    }
}
