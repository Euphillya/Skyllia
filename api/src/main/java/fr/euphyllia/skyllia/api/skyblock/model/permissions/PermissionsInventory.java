package fr.euphyllia.skyllia.api.skyblock.model.permissions;

import io.papermc.paper.annotation.DoNotUse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents various inventory permissions that can be applied to a Skyblock island.
 */
public enum PermissionsInventory implements Permissions {
    OPEN_CHEST(1),
    OPEN_ANVIL(2),
    OPEN_WORKBENCH(4),
    OPEN_ENCHANTING(8),
    OPEN_BREWING(16),
    OPEN_SMITHING(32),
    OPEN_BEACON(64),
    OPEN_SHULKER_BOX(128),
    OPEN_FURNACE(256),
    OPEN_LECTERN(512),
    OPEN_CRAFTER(1_024), // 1.20.4
    OPEN_LOOM(2_048),
    OPEN_GRINDSTONE(4_096),
    OPEN_STONECUTTER(8_192),
    OPEN_CARTOGRAPHY(16_384),
    OPEN_MERCHANT(32_768),
    OPEN_HOPPER(65_536),
    OPEN_BARREL(131_072),
    OPEN_BLAST_FURNACE(262_144),
    OPEN_SMOKER(524_288),
    @Deprecated
    OPEN_SMITHING_NEW(1_048_576), // It's an old permission
    OPEN_DISPENSER(2_097_152),
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
}
