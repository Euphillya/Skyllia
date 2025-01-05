package fr.euphyllia.skyllia.api.skyblock.model.permissions;

/**
 * Represents a set of permissions for a Skyblock island.
 */
public interface Permissions {

    /**
     * Gets the value of the permission as a long.
     *
     * @return The permission value.
     */
    long getPermissionValue();

    /**
     * Gets the type of the permission.
     *
     * @return The permission type.
     */
    PermissionsType getPermissionType();

    String getName();
}
