package fr.euphyllia.skyllia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents various command permissions that can be applied to a Skyblock island.
 */
public enum PermissionsCommandIsland implements Permissions {
    DEMOTE(1),
    PROMOTE(2),
    KICK(4),
    ACCESS(8),
    SET_HOME(16),
    INVITE(32),
    SET_BIOME(64),
    SET_WARP(128),
    DEL_WARP(256),
    TP_WARP(512),
    EXPEL(1_024),
    MANAGE_PERMISSION(2_048),
    BAN(4_096),
    UNBAN(8_192),
    MANAGE_TRUST(16_384),
    MANAGE_GAMERULE(32_768);

    private final long permissionValue;

    /**
     * Constructs a PermissionsCommandIsland with the specified permission value.
     *
     * @param permissionLong The permission value.
     */
    PermissionsCommandIsland(long permissionLong) {
        this.permissionValue = permissionLong;
    }

    /**
     * Converts a string representation of permission names to a long value.
     *
     * @param names The string representation of permission names.
     * @return The long value of the permissions.
     */
    public static long permissionValue(String names) {
        try {
            return PermissionsIsland.valueOf(names).getPermissionValue();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Gets the maximum value of all permissions combined.
     *
     * @return The maximum permissions value.
     */
    public static long maxPermissionsValue() {
        return Arrays.stream(PermissionsIsland.values()).mapToLong(PermissionsIsland::getPermissionValue).sum();
    }

    /**
     * Gets a list of all permission names.
     *
     * @return A list of permission names.
     */
    public static List<String> getListPermissions() {
        List<String> list = new ArrayList<>();
        for (PermissionsIsland permissionsIsland : PermissionsIsland.values()) {
            String name = permissionsIsland.name();
            list.add(name);
        }
        return list;
    }

    /**
     * Gets the permission value of this command permission.
     *
     * @return The permission value.
     */
    @Override
    public long getPermissionValue() {
        return this.permissionValue;
    }

    /**
     * Gets the type of the permission.
     *
     * @return The permission type.
     */
    @Override
    public PermissionsType getPermissionType() {
        return PermissionsType.COMMANDS;
    }
}
