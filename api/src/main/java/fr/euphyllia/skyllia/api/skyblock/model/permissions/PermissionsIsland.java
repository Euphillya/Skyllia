package fr.euphyllia.skyllia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents various permissions that can be applied to a Skyblock island.
 */
public enum PermissionsIsland implements Permissions {
    BLOCK_BREAK(1),
    BLOCK_PLACE(2),
    BUCKETS(4),
    REDSTONE(8),
    PVP(16),
    KILL_MONSTER(32),
    KILL_ANIMAL(64),
    DROP_ITEMS(128),
    PICKUP_ITEMS(256),
    USE_NETHER_PORTAL(512),
    USE_END_PORTAL(1_024),
    INTERACT_ENTITIES(2_048),
    KILL_UNKNOWN_ENTITY(4_096),
    KILL_NPC(8_192),
    INTERACT(16_384);

    private final long permissionValue;

    /**
     * Constructs a PermissionsIsland with the specified permission value.
     *
     * @param permissionLong The permission value.
     */
    PermissionsIsland(long permissionLong) {
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
     * Gets the permission value of this island permission.
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
        return PermissionsType.ISLAND;
    }
}
