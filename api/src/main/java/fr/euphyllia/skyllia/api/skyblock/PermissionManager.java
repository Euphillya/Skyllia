package fr.euphyllia.skyllia.api.skyblock;

import fr.euphyllia.skyllia.api.skyblock.model.permissions.Permissions;
import fr.euphyllia.skyllia.api.skyblock.model.permissions.PermissionsIsland;
import fr.euphyllia.skyllia.api.utils.BitwiseFlag;

/**
 * Manages permissions for a Skyblock island using bitwise flags.
 */
public class PermissionManager extends BitwiseFlag {

    /**
     * Constructs a new PermissionManager with the specified initial flags.
     *
     * @param actualFlags The initial set of flags representing permissions.
     */
    public PermissionManager(long actualFlags) {
        this.flags = actualFlags;
    }

    /**
     * Converts a string representation of permissions to a long value.
     *
     * @param permissions The string representation of permissions.
     * @return The long value of the permissions.
     */
    public static long valueOf(String permissions) {
        return PermissionsIsland.permissionValue(permissions);
    }

    /**
     * Gets the current set of permissions as a long value.
     *
     * @return The current permissions.
     */
    public long getPermissions() {
        return this.flags;
    }

    /**
     * Defines a specific permission.
     *
     * @param permission The permission to set.
     * @param value True to enable the permission, false to disable it.
     */
    public void definePermission(long permission, boolean value) {
        this.setFlags(permission, value);
    }

    /**
     * Checks if a specific permission is set.
     *
     * @param permission The permission to check.
     * @return True if the permission is set, false otherwise.
     */
    public boolean hasPermission(long permission) {
        return this.isFlagSet(permission);
    }

    /**
     * Checks if a specific permission is set using the Permissions object.
     *
     * @param permissionsIsland The Permissions object to check.
     * @return True if the permission is set, false otherwise.
     */
    public boolean hasPermission(Permissions permissionsIsland) {
        return this.isFlagSet(permissionsIsland.getPermissionValue());
    }
}
