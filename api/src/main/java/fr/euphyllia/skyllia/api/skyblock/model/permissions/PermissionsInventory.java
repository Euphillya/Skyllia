package fr.euphyllia.skyllia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents various inventory permissions that can be applied to a Skyblock island.
 */
public enum PermissionsInventory implements Permissions {
    /**
     * Permission to open chests.
     */
    OPEN_CHEST(1),

    /**
     * Permission to open anvils.
     */
    OPEN_ANVIL(1 << 1),

    /**
     * Permission to open workbenches.
     */
    OPEN_WORKBENCH(1 << 2),

    /**
     * Permission to open enchanting tables.
     */
    OPEN_ENCHANTING(1 << 3),

    /**
     * Permission to open brewing stands.
     */
    OPEN_BREWING(1 << 4),

    /**
     * Permission to open smithing tables.
     */
    OPEN_SMITHING(1 << 5),

    /**
     * Permission to open beacons.
     */
    OPEN_BEACON(1 << 6),

    /**
     * Permission to open shulker boxes.
     */
    OPEN_SHULKER_BOX(1 << 7),

    /**
     * Permission to open furnaces.
     */
    OPEN_FURNACE(1 << 8),

    /**
     * Permission to open lecterns.
     */
    OPEN_LECTERN(1 << 9),

    /**
     * Permission to open crafting tables.
     */
    OPEN_CRAFTER(1 << 10),

    /**
     * Permission to open looms.
     */
    OPEN_LOOM(1 << 11),

    /**
     * Permission to open grindstones.
     */
    OPEN_GRINDSTONE(1 << 12),

    /**
     * Permission to open stonecutters.
     */
    OPEN_STONECUTTER(1 << 13),

    /**
     * Permission to open cartography tables.
     */
    OPEN_CARTOGRAPHY(1 << 14),

    /**
     * Permission to open merchants.
     */
    OPEN_MERCHANT(1 << 15),

    /**
     * Permission to open hoppers.
     */
    OPEN_HOPPER(1 << 16),

    /**
     * Permission to open barrels.
     */
    OPEN_BARREL(1 << 17),

    /**
     * Permission to open blast furnaces.
     */
    OPEN_BLAST_FURNACE(1 << 18),

    /**
     * Permission to open smokers.
     */
    OPEN_SMOKER(1 << 19),

    /**
     * @deprecated Permission to open the new smithing tables (old permission).
     * This permission is deprecated and should not be used.
     */
    @Deprecated
    OPEN_SMITHING_NEW(1 << 20), // It's an old permission

    /**
     * Permission to open dispensers.
     */
    OPEN_DISPENSER(1 << 21),

    /**
     * Permission to open droppers.
     */
    OPEN_DROPPER(1 << 22);

    private final long permissionValue;

    /**
     * Constructs a PermissionsInventory with the specified permission value.
     *
     * @param permissionLong The permission value.
     */
    PermissionsInventory(long permissionLong) {
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
            return PermissionsInventory.valueOf(names).getPermissionValue();
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
        return Arrays.stream(PermissionsInventory.values()).mapToLong(PermissionsInventory::getPermissionValue).sum();
    }

    /**
     * Gets a list of all permission names.
     *
     * @return A list of permission names.
     */
    public static List<String> getListPermissions() {
        List<String> list = new ArrayList<>();
        for (PermissionsInventory permissionsInventory : PermissionsInventory.values()) {
            list.add(permissionsInventory.name());
        }
        return list;
    }

    /**
     * Gets the permission value of this inventory permission.
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
        return PermissionsType.INVENTORY;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
