package fr.euphyllia.skyfolia.managers.skyblock;

import fr.euphyllia.skyfolia.api.skyblock.model.PermissionsIsland;
import fr.euphyllia.skyfolia.api.utils.BitwiseFlag;

public class PermissionManager extends BitwiseFlag {

    public PermissionManager(int actualFlags) {
        this.flags = actualFlags;
    }

    public static int valueOf(String permissions) {
        return PermissionsIsland.permissionValue(permissions);
    }

    public int getPermissions() {
        return this.flags;
    }

    public void definePermission(int permission, boolean value) {
        this.setFlags(permission, value);
    }

    public boolean hasPermission(int permission) {
        return this.isFlagSet(permission);
    }

    public boolean hasPermission(PermissionsIsland permissionsIsland) {
        return this.isFlagSet(permissionsIsland.getPermissionValue());
    }
}
