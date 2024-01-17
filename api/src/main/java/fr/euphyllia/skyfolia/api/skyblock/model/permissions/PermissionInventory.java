package fr.euphyllia.skyfolia.api.skyblock.model.permissions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum PermissionInventory implements Permissions {
    DEFAULT(0),
    OPEN_CHEST(1),
    USE_ANVIL(2),
    USE_WORKBENCH(4),
    USE_ENCHANTING_TABLE(8),
    USE_BREWING_TABLE(16),
    USE_SMITING(32),
    USE_BEACON(64),
    OPEN_SHULKER(128),
    USE_FURNACE(256),
    USE_LECTERN(512),
    OPEN_CRAFTER(1_024),
    USE_LOOM(2_048),
    USE_GRINDSTONE(4_096),
    USE_STONECUTTER(8_192),
    OPEN_ENDERCHEST(16_384),
    OPEN_CARTOGRAPHY(32_768),
    OPEN_MERCHANT(65_536);

    private final long permissionValue;

    PermissionInventory(long permissionLong) {
        this.permissionValue = permissionLong;
    }

    public static long permissionValue(String names) {
        try {
            return PermissionsIsland.valueOf(names).getPermissionValue();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    public static long maxPermissionsValue() {
        return Arrays.stream(PermissionsIsland.values()).mapToLong(PermissionsIsland::getPermissionValue).sum();
    }

    public static List<String> getListPermissions() {
        List<String> list = new ArrayList<>();
        for (PermissionsIsland permissionsIsland : PermissionsIsland.values()) {
            String name = permissionsIsland.name();
            list.add(name);
        }
        return list;
    }

    @Override
    public long getPermissionValue() {
        return this.permissionValue;
    }

    @Override
    public PermissionsType getPermissionType() {
        return PermissionsType.INVENTORY;
    }
}
