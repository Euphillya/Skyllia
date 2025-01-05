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
    OPEN_ANVIL(2),

    /**
     * Permission to open workbenches.
     */
    OPEN_WORKBENCH(4),

    /**
     * Permission to open enchanting tables.
     */
    OPEN_ENCHANTING(8),

    /**
     * Permission to open brewing stands.
     */
    OPEN_BREWING(16),

    /**
     * Permission to open smithing tables.
     */
    OPEN_SMITHING(32),

    /**
     * Permission to open beacons.
     */
    OPEN_BEACON(64),

    /**
     * Permission to open shulker boxes.
     */
    OPEN_SHULKER_BOX(128),

    /**
     * Permission to open furnaces.
     */
    OPEN_FURNACE(256),

    /**
     * Permission to open lecterns.
     */
    OPEN_LECTERN(512),

    /**
     * Permission to open crafting tables.
     */
    OPEN_CRAFTER(1_024), // 1.20.4

    /**
     * Permission to open looms.
     */
    OPEN_LOOM(2_048),

    /**
     * Permission to open grindstones.
     */
    OPEN_GRINDSTONE(4_096),

    /**
     * Permission to open stonecutters.
     */
    OPEN_STONECUTTER(8_192),

    /**
     * Permission to open cartography tables.
     */
    OPEN_CARTOGRAPHY(16_384),

    /**
     * Permission to open merchants.
     */
    OPEN_MERCHANT(32_768),

    /**
     * Permission to open hoppers.
     */
    OPEN_HOPPER(65_536),

    /**
     * Permission to open barrels.
     */
    OPEN_BARREL(131_072),

    /**
     * Permission to open blast furnaces.
     */
    OPEN_BLAST_FURNACE(262_144),

    /**
     * Permission to open smokers.
     */
    OPEN_SMOKER(524_288),

    /**
     * @deprecated Permission to open the new smithing tables (old permission).
     * This permission is deprecated and should not be used.
     */
    @Deprecated
    OPEN_SMITHING_NEW(1_048_576), // It's an old permission

    /**
     * Permission to open dispensers.
     */
    OPEN_DISPENSER(2_097_152),

    /**
     * Permission to open droppers.
     */
    OPEN_DROPPER(4_194_304);

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
