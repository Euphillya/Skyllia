package fr.euphyllia.skyllia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents various permissions that can be applied to a Skyblock island.
 */
public enum PermissionsIsland implements Permissions {
    /**
     * Permission to break blocks.
     */
    BLOCK_BREAK(1),

    /**
     * Permission to place blocks.
     */
    BLOCK_PLACE(1 << 1),

    /**
     * Permission to use buckets.
     */
    BUCKETS(1 << 2),

    /**
     * Permission to use redstone components.
     */
    REDSTONE(1 << 3),

    /**
     * Permission to engage in player vs player (PvP) combat.
     */
    PVP(1 << 4),

    /**
     * Permission to kill monsters.
     */
    KILL_MONSTER(1 << 5),

    /**
     * Permission to kill animals.
     */
    KILL_ANIMAL(1 << 6),

    /**
     * Permission to drop items.
     */
    DROP_ITEMS(1 << 7),

    /**
     * Permission to pick up items.
     */
    PICKUP_ITEMS(1 << 8),

    /**
     * Permission to use nether portals.
     */
    USE_NETHER_PORTAL(1 << 9),

    /**
     * Permission to use end portals.
     */
    USE_END_PORTAL(1 << 10),

    /**
     * Permission to interact with entities.
     */
    INTERACT_ENTITIES(1 << 11),

    /**
     * Permission to kill unknown entities.
     */
    KILL_UNKNOWN_ENTITY(1 << 12),

    /**
     * Permission to kill NPCs (non-player characters).
     */
    KILL_NPC(1 << 13),

    /**
     * Permission to perform general interactions on the island.
     */
    INTERACT(1 << 14),

    /**
     * Permission to teleport on the island.
     */
    TELEPORT(1 << 15),
    /**
     * Permission to use ender pearls.
     */
    USE_ENDER_PEARL(1 << 16);

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

    @Override
    public String getName() {
        return this.name();
    }
}
