package fr.euphyllia.skyllia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents various command permissions that can be applied to a Skyblock island.
 */
public enum PermissionsCommandIsland implements Permissions {
    /**
     * Permission to demote players.
     */
    DEMOTE(1),

    /**
     * Permission to promote players.
     */
    PROMOTE(2),

    /**
     * Permission to kick players from the island.
     */
    KICK(4),

    /**
     * Permission to access certain island features.
     */
    ACCESS(8),

    /**
     * Permission to set a home location on the island.
     */
    SET_HOME(16),

    /**
     * Permission to invite players to the island.
     */
    INVITE(32),

    /**
     * Permission to set a biome on the island.
     */
    SET_BIOME(64),

    /**
     * Permission to set a warp point on the island.
     */
    SET_WARP(128),

    /**
     * Permission to delete a warp point from the island.
     */
    DEL_WARP(256),

    /**
     * Permission to teleport to a warp point on the island.
     */
    TELEPORT_WARP(512),

    /**
     * Permission to expel players from the island.
     */
    EXPEL(1_024),

    /**
     * Permission to manage island permissions.
     */
    MANAGE_PERMISSION(2_048),

    /**
     * Permission to ban players from the island.
     */
    BAN(4_096),

    /**
     * Permission to unban players from the island.
     */
    UNBAN(8_192),

    /**
     * Permission to manage trust levels on the island.
     */
    MANAGE_TRUST(16_384),

    /**
     * Permission to manage game rules on the island.
     */
    MANAGE_GAMERULE(32_768),

    /**
     * Permission to debug permission on the island.
     */
    DEBUG(65_536);

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

    @Override
    public String getName() {
        return this.name();
    }
}
